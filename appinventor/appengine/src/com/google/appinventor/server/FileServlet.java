package com.google.appinventor.server;

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
import java.util.*;
import java.util.zip.*;

import org.json.*;

public class FileServlet extends HttpServlet {
    private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
    private final FileImporter fileImporter = new FileImporterImpl();
    private final FileExporter fileExporter = new FileExporterImpl();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("text/html; charset=utf-8");

        String action = req.getParameter("action");
        if (action == null)
            action = "";
        switch (action) {
            case "userProjects": {
                String uid = req.getParameter("uid");
                if (isNullOrEmpty(uid))
                    return;
                
                resp.getWriter().println(getUserProjects(uid));
                break;
            }
            case "projectFiles": {
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
                String users = req.getParameter("users");
                attachDownloadData(resp, exportAllProjects(users));
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
            default: {
                JSONObject json = new JSONObject();
                for (AdminUser user : storageIo.searchUsers("")) {
                    String uid = user.getId();
                    json.put(uid, getUserProjects(uid));
                }
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
                if(isNullOrEmpty(users)||isNullOrEmpty(name)||(isNullOrEmpty(encodedContent)))
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

                    try{
                        ByteArrayInputStream bin = new ByteArrayInputStream(content);
                        fileImporter.importProject(uid, importName, bin, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                        return;
                    }
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
            for (AdminUser user : storageIo.searchUsers("")) {
                String uid = user.getId();
                if((targets != null) && (!targets.contains(uid)))
                    continue;
                String email = user.getEmail();
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