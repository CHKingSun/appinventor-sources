package com.google.appinventor.server;

import com.google.appinventor.server.storage.SQLStorageIo;
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
import java.sql.Connection;
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
                for (String uid : storageIo.listUsers()){
                    User user = storageIo.getUser(uid);
                    out.printf("%s,%s\n", user.getUserEmail(), user.getUserName());
                }
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

                if(storageIo instanceof SQLStorageIo){
                    SQLStorageIo sqlStorageIo = (SQLStorageIo)storageIo;
                    Connection conn = sqlStorageIo.getConnection();
                    sqlStorageIo.beginTransaction(conn);
                }

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

                if(storageIo instanceof SQLStorageIo){
                    SQLStorageIo sqlStorageIo = (SQLStorageIo)storageIo;
                    Connection conn = sqlStorageIo.getConnection();
                    try {
                        conn.commit();
                    }catch(Exception e){}
                    sqlStorageIo.endTransaction(conn);
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
                String usersJSON = req.getParameter("users");
                if(isNullOrEmpty(usersJSON))
                    return;

                if(storageIo instanceof SQLStorageIo){
                    SQLStorageIo sqlStorageIo = (SQLStorageIo)storageIo;
                    Connection conn = sqlStorageIo.getConnection();
                    sqlStorageIo.beginTransaction(conn);
                }
                
                JSONArray users = new JSONArray(usersJSON);
                for (int i = 0; i < users.length(); i++) {
                    String uid = users.getString(i);
                    /* for(long pid : storageIo.getProjects(uid))
                        storageIo.deleteProject(uid, pid); */
                    storageIo.removeUser(uid);
                }

                if(storageIo instanceof SQLStorageIo){
                    SQLStorageIo sqlStorageIo = (SQLStorageIo)storageIo;
                    Connection conn = sqlStorageIo.getConnection();
                    try {
                        conn.commit();
                    }catch(Exception e){}
                    sqlStorageIo.endTransaction(conn);
                }

                out.print("OK");
                break;
            }
            case "deleteProjects": {
                String projectsJSON = req.getParameter("projects");
                if(isNullOrEmpty(projectsJSON))
                    return;
                
                JSONArray projects = new JSONArray(projectsJSON);
                for(int i=0;i<projects.length();i++){
                    JSONObject project = projects.getJSONObject(i);
                    storageIo.deleteProject(project.getString("uid"), project.getLong("pid"));
                }
                out.print("OK");
                break;
            }
            case "createGroup": {
                String name = req.getParameter("name");
                if(isNullOrEmpty(name))
                    return;
                if(storageIo.findGroupByName(name) != 0){
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
                String usersJSON = req.getParameter("users");
                if(isNullOrEmpty(usersJSON))
                    return;

                JSONArray users = new JSONArray(usersJSON);
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < users.length(); i++)
                    list.add(users.getString(i));
                storageIo.addUsersToGroup(gid, list);
                out.print("OK");
                break;
            }
            case "removeUsersFromGroup": {
                long gid = Long.parseLong(req.getParameter("gid"));
                String usersJSON = req.getParameter("users");
                if(isNullOrEmpty(usersJSON))
                    return;
                
                JSONArray users = new JSONArray(usersJSON);
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < users.length(); i++)
                    list.add(users.getString(i));
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
