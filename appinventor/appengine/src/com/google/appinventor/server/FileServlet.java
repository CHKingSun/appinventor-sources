package com.google.appinventor.server;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.admin.AdminUser;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.Nonce;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.*;

import org.json.*;

public class FileServlet extends HttpServlet {
    private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
    private final FileImporter fileImporter = new FileImporterImpl();
    private final FileExporter fileExporter = new FileExporterImpl();
    private static final Flag<String> imagesPath = Flag.createFlag("images.path", "");

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("text/html; charset=utf-8");

        String action = req.getParameter("action");
        if (action == null)
            action = "";
        switch (action) {
            case "listUserProjects": {
                String uid = req.getParameter("uid");
                if (isNullOrEmpty(uid))
                    return;
                
                resp.getWriter().println(getUserProjects(uid));
                break;
            }
            case "listProjectFiles": {
                String uid = req.getParameter("uid");
                long pid = Long.parseLong(req.getParameter("pid"));
                if (isNullOrEmpty(uid))
                    return;
                
                JSONObject json = new JSONObject();
                json.put("uid", uid);
                json.put("pid", pid);
                json.put("sources", storageIo.getProjectSourceFiles(uid, pid));
                json.put("outputs", storageIo.getProjectOutputFiles(uid, pid));
                resp.getWriter().println(json);
                break;
            }
            case "exportFile": {
                String uid = req.getParameter("uid");
                long pid = Long.parseLong(req.getParameter("pid"));
                String path = req.getParameter("path");
                if(isNullOrEmpty(uid)||isNullOrEmpty(path))
                    return;
                
                attachDownloadData(resp, exportFile(uid, pid, path));
                break;
            }
            case "exportProject": {
                String uid = req.getParameter("uid");
                long pid = Long.parseLong(req.getParameter("pid"));
                if (isNullOrEmpty(uid))
                    return;
                
                attachDownloadData(resp, exportProject(uid, pid));
                break;
            }
            case "exportAllProjectsForUser": {
                String uid = req.getParameter("uid");
                if (isNullOrEmpty(uid))
                    return;
                
                attachDownloadData(resp, exportAllProjectsForUser(uid));
                break;
            }
            case "exportAllProjects": {
                String usersJSON = req.getParameter("users");
                attachDownloadData(resp, exportAllProjects(usersJSON));
                break;
            }
            case "exportProjectsBatched": {
                String projectsJSON = req.getParameter("projects");
                if (isNullOrEmpty(projectsJSON))
                    return;
                
                attachDownloadData(resp, exportProjectsBatched(projectsJSON));
                break;
            }
            case "getSharedProject": {
                String nonceValue = req.getParameter("nonce");
                if(isNullOrEmpty(nonceValue))
                    return;

                Nonce nonce = storageIo.getNoncebyValue(nonceValue);
                if (nonce != null)
                    attachDownloadData(resp, exportProject(nonce.getUserId(), nonce.getProjectId()));
                else
                    resp.getWriter().print("分享链接已过期或项目不存在");
                break;
            }
            case "getScreenShot": {
                String uid = req.getParameter("uid");
                String pid = req.getParameter("pid");
                if (!isNullOrEmpty(uid)) {
                    String[] pngFiles = Paths.get(imagesPath.get(), uid, pid).toFile()
                            .list(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith("png");
                        }
                    });
                    if (pngFiles != null && pngFiles.length > 0) {
//                        System.out.println("../images/" + uid + "/" + pid + "/" + pngFiles[0]);
                        resp.getWriter().println("../images/" + uid + "/" + pid + "/" + pngFiles[0]);
                        break;
                    }
                }

                resp.getWriter().println("../images/squairy_light.png");
                break;
            }
            case "importProjects": {
                String uid = req.getParameter("uid");
                for (File dir : new File("C:\\Users\\KingSun\\Desktop\\G0").listFiles()) {
                    File file = dir.listFiles( new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".aia");
                        }
                    })[0];
                    try {
                        fileImporter.importProject(uid, file.getName(), new FileInputStream(file));
                    } catch (FileImporterException e) {
                        resp.getWriter().println(e.getLocalizedMessage());
                        break;
                    }
                }
                resp.getWriter().println("OK");
                break;
            }
            default: {
                JSONObject json = new JSONObject();
                for (String uid : storageIo.listUsers()) 
                    json.put(uid, getUserProjects(uid));
                resp.getWriter().println(json);
                break;
            }
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("text/html; charset=utf-8");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");
        if (action == null)
            action = "";
        switch (action) {
            case "importProject": {
                String users = req.getParameter("users");
                String name = req.getParameter("name");
                String encodedContent = req.getParameter("content");
                if(isNullOrEmpty(users)||isNullOrEmpty(name)||isNullOrEmpty(encodedContent))
                    return;
                
                byte content[] = null;
                try {
                    content = Base64.decodeBase64(encodedContent);
                } catch (Exception e) {
                    e.printStackTrace();
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                    return;
                }
                
                JSONArray json = new JSONArray(users);
                for(int i=0;i<json.length();i++){
                    String uid = json.getString(i);
                    String importName = name;
                    for(long pid : storageIo.getProjects(uid))
                        if(storageIo.getProjectName(uid, pid).equals(name))
                            importName += "_copy";

                    importProject(uid, importName, content);
                }
                out.print("OK");
                break;
            }
            case "importProjectZip": {
                String users = req.getParameter("users");
                String encodedContent = req.getParameter("content");
                if(isNullOrEmpty(users)||isNullOrEmpty(encodedContent))
                    return;
                
                JSONArray json = new JSONArray(users);
                byte content[] = null;
                try {
                    content = Base64.decodeBase64(encodedContent);
                } catch (Exception e) {
                    e.printStackTrace();
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                    return;
                }
                
                ByteArrayInputStream bin = new ByteArrayInputStream(content);
                ZipInputStream zin = new ZipInputStream(bin);
                ZipEntry entry = zin.getNextEntry();
                while(entry != null){
                    String file = entry.getName();
                    if(file.split("\\.")[1].equals("aia")){
                        String path = file.split("\\.")[0];
                        path = path.replace("/", "_");
                        
                        ByteArrayOutputStream buf = new ByteArrayOutputStream();
                        byte b[] = new byte[1024];
                        int len = zin.read(b, 0, 1024);
                        while(len != -1){
                            buf.write(b, 0, len);
                            len = zin.read(b, 0, 1024);
                        }
                        
                        for(int i=0;i<json.length();i++){
                            String uid = json.getString(i);
                            importProject(uid, path, buf.toByteArray());
                        }
                    }
                    zin.closeEntry();
                    entry = zin.getNextEntry();
                }
                out.print("OK");
                break;
            }
            case "shareProject": {
                String uid = req.getParameter("uid");
                long pid = Long.parseLong(req.getParameter("pid"));
                if(isNullOrEmpty(uid))
                    return;
                
                String name = uid + pid;
                String nonceValue = new String(Base64.encodeBase64(name.getBytes("UTF-8")), "UTF-8");
                storageIo.storeNonce(nonceValue, uid, pid);
                out.print(nonceValue);
                break;
            }
        }
    }
    
    private void importProject(String uid, String name, byte content[]){
        try{
            ByteArrayInputStream bin = new ByteArrayInputStream(content);
            fileImporter.importProject(uid, name, bin, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONArray getUserProjects(String uid) {
        JSONArray json = new JSONArray();
        for (long pid : storageIo.getProjects(uid)) {
            JSONObject obj = new JSONObject();
            obj.put("uid", uid);
            obj.put("pid", pid);
            obj.put("name", storageIo.getProjectName(uid, pid));
            obj.put("dateCreated", storageIo.getProjectDateCreated(uid, pid));
            obj.put("dateModified", storageIo.getProjectDateModified(uid, pid));
            json.put(obj);
        }
        return json;
    }

    private RawFile exportFile(String uid, long pid, String path) {
        RawFile file = null;
        try {
            file = fileExporter.exportFile(uid, pid, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private RawFile exportProject(String uid, long pid) {
        RawFile srcFile = null;
        try {
            ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(uid, pid, false, false, null, false, false, false, false);
            srcFile = zipFile.getRawFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return srcFile;
    }

    private RawFile exportAllProjectsForUser(String uid) {
        String email = storageIo.getUser(uid).getUserEmail();
        RawFile file = null;
        try {
            ProjectSourceZip zipFile = fileExporter.exportAllProjectsSourceZip(uid, "all-projects-" + email + ".zip");
            file = zipFile.getRawFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private RawFile exportAllProjects(String users) {
        List<String> targets = null;
        if(!isNullOrEmpty(users)){
            targets = new ArrayList<String>();
            JSONArray json = new JSONArray(users);
            for(int i=0;i<json.length();i++)
                targets.add(json.getString(i));
        }
        
        ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(zipFile)) {
            for (String uid : storageIo.listUsers()) {
                if((targets != null) && (!targets.contains(uid)))
                    continue;
                User user = storageIo.getUser(uid);
                String email = user.getUserEmail();
                for (long pid : storageIo.getProjects(uid)) {
                    RawFile file = exportProject(uid, pid);
                    out.putNextEntry(new ZipEntry(email + "/" + file.getFileName()));
                    out.write(file.getContent());
                    out.closeEntry();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RawFile("all-projects.zip", zipFile.toByteArray());
    }
    
    private RawFile exportProjectsBatched(String projects){
        ByteArrayOutputStream zipFile = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(zipFile)) {
            JSONArray json = new JSONArray(projects);
            for(int i=0;i<json.length();i++){
                JSONObject obj = json.getJSONObject(i);
                String uid = obj.getString("uid");
                long pid = obj.getLong("pid");

                User user = storageIo.getUser(uid);
                String email = user.getUserEmail();
                RawFile file = exportProject(uid, pid);
                out.putNextEntry(new ZipEntry(email + "/" + file.getFileName()));
                out.write(file.getContent());
                out.closeEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new RawFile("all-projects.zip", zipFile.toByteArray());
    }

    private void attachDownloadData(HttpServletResponse resp, RawFile file) {
        if (file == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = file.getFileName();
        byte content[] = file.getContent();

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("content-disposition", "attachment; filename=\"" + fileName + "\"");
        resp.setContentType(StorageUtil.getContentTypeForFilePath(fileName));
        resp.setContentLength(content.length);

        try (ServletOutputStream out = resp.getOutputStream()) {
            out.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static boolean isNullOrEmpty(String str){
        return (str == null) || str.equals("");
    }
}