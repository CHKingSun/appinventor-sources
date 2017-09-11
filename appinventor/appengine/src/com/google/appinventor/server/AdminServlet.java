package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.admin.AdminUser;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.server.util.PasswordHash;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.util.*;

import org.json.*;

public class AdminServlet extends HttpServlet {
    private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("text/html; charset=utf-8");
        PrintWriter out = resp.getWriter();
        
        String action = req.getParameter("action");
        if (action == null)
            action = "";
        switch (action) {
            case "exportUsersCSV": {
                resp.setHeader("content-disposition", "attachment; filename=\"users.csv\"");
                resp.setContentType("text/plain;charset=utf-8");
                for (AdminUser user : storageIo.searchUsers(""))
                    out.printf("%s,%s\n", user.getEmail(), user.getName());
                break;
            }
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("text/html; charset=utf-8");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");
        if (action == null)
            action = "";
        switch (action) {
            case "importUsersCSV": {
                String content = req.getParameter("content");
                if(isNullOrEmpty(content))
                    return;
                
                int count = 0;
                for(String row : content.split("\\n")){
                    String parts[] = row.split(",");
                    String email = parts[0];
                    String name = (parts.length>1) ? parts[1] : email;
                    String password = (parts.length>2) ? parts[2] : "123456";
                    
                    User user = storageIo.getUserFromEmail(email);
                    String hash = user.getPassword();
                    if ((hash == null) || hash.equals("")) {
                        String hashedPassword = "";
                        try {
                            hashedPassword = PasswordHash.createHash(password);
                        } catch (Exception e) {
                            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                            return;
                        }
                        storageIo.setUserPassword(user.getUserId(), hashedPassword);
                        storageIo.setUserName(user.getUserId(), name);
                        count++;
                    }
                }
                out.print("成功导入" + count + "条记录");
                break;
            }
            case "passwordReset": {
                String uid = req.getParameter("uid");
                String password = req.getParameter("password");
                if (isNullOrEmpty(uid))
                    return;
                if(isNullOrEmpty(password))
                    return;
                
                User user = storageIo.getUser(uid);
                String hashedPassword = "";
                try {
                    hashedPassword = PasswordHash.createHash(password);
                } catch (Exception e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                    return;
                }
                storageIo.setUserPassword(uid, hashedPassword);
                out.print("OK");
                break;
            }
            case "removeUsers": {
                String users = req.getParameter("users");
                if(isNullOrEmpty(users))
                    return;
                
                JSONArray json = new JSONArray(users);
                for (int i = 0; i < json.length(); i++) {
                    String uid = json.getString(i);
                    for(long pid : storageIo.getProjects(uid))
                        storageIo.deleteProject(uid, pid);
                    storageIo.removeUser(uid);
                }
                out.print("OK");
                break;
            }
            case "deleteProject": {
                String uid = req.getParameter("uid");
                long pid = Long.parseLong(req.getParameter("pid"));
                if(isNullOrEmpty(uid))
                    return;
                
                storageIo.deleteProject(uid, pid);
                out.print("OK");
                break;
            }
            case "createGroup": {
                String name = req.getParameter("name");
                if(isNullOrEmpty(name))
                    return;
                if(storageIo.getGroupByName(name) != 0){
                    out.print("存在同名分组");
                    return;
                }
                storageIo.createGroup(name);
                out.print("OK");
                break;
            }
            case "removeGroup": {
                long gid = Long.parseLong(req.getParameter("gid"));
                storageIo.removeGroup(gid);
                out.print("OK");
                break;
            }
            case "addUsersToGroup": {
                long gid = Long.parseLong(req.getParameter("gid"));
                String users = req.getParameter("users");
                if(isNullOrEmpty(users))
                    return;

                JSONArray json = new JSONArray(users);
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < json.length(); i++)
                    list.add(json.getString(i));
                storageIo.addUsersToGroup(gid, list);
                out.print("OK");
                break;
            }
            case "removeUsersFromGroup": {
                long gid = Long.parseLong(req.getParameter("gid"));
                String users = req.getParameter("users");
                if(isNullOrEmpty(users))
                    return;
                
                JSONArray json = new JSONArray(users);
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < json.length(); i++)
                    list.add(json.getString(i));
                storageIo.removeUsersFromGroup(gid, list);
                out.print("OK");
                break;
            }
        }
    }
    
    private static boolean isNullOrEmpty(String str){
        return (str == null) || str.equals("");
    }
}
