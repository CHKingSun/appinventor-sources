package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.admin.AdminUser;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.server.util.PasswordHash;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;
import java.util.*;

import org.json.*;

public class UserServlet extends HttpServlet {
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
            case "listGroups": {
                JSONArray json = new JSONArray();
                for (long gid : storageIo.listGroups()) {
                    JSONObject obj = new JSONObject();
                    obj.put("gid", gid);
                    obj.put("name", storageIo.getGroupName(gid));
                    json.put(obj);
                }
                out.println(json);
                break;
            }
            case "listGroupUsers": {
                long gid = Long.parseLong(req.getParameter("gid"));
                JSONArray json = new JSONArray();
                for (String uid : storageIo.getGroupUsers(gid))
                    json.put(getUserInfoJSON(uid));
                out.println(json);
                break;
            }
            case "openProject":{
                String uid = req.getParameter("uid");
                if(isNullOrEmpty(uid)){
                    out.print("账号不能为空");
                    return;
                }
                User user = storageIo.getUser(uid);
                long pid = 0;
                if(isNullOrEmpty(req.getParameter("pid"))){
                    String pname = req.getParameter("pname");
                    if(isNullOrEmpty(pname)){
                        out.print("项目名不能为空");
                        return;
                    }
                    for(long p : storageIo.getProjects(uid)){
                        UserProject project = storageIo.getUserProject(uid, p);
                        if(pname.equals(project.getProjectName())){
                            pid = p;
                            break;
                        }
                    }
                }
                else
                    pid = Long.parseLong(req.getParameter("pid"));
                if(storageIo.getUserProject(uid, pid) == null){
                    out.print("项目不存在");
                    return;
                }
                
                String encodedSettings = storageIo.loadSettings(uid);
                if(isNullOrEmpty(encodedSettings))
                    encodedSettings = "{\"GeneralSettings\":{\"CurrentProjectId\":\"0\",\"DisabledUserUrl\":\"\",\"TemplateUrls\":\"\"},\"SimpleSettings\":{},\"SplashSettings\":{\"DeclinedSurvey\":\"\",\"ShowSurvey\":\"0\",\"SplashVersion\":\"0\"},\"BlocksSettings\":{\"Grid\":\"false\",\"Snap\":\"false\"}}";
                
                JSONObject obj = new JSONObject(encodedSettings);
                JSONObject generalSettings = obj.getJSONObject("GeneralSettings");
                if(generalSettings == null)
                    generalSettings = new JSONObject();
                generalSettings.put("CurrentProjectId", String.valueOf(pid));
                obj.put("GeneralSettings", generalSettings);
                
                storageIo.storeSettings(uid, obj.toString());
                resp.sendRedirect("/");
                break;
            }
            default: {
                String uid = req.getParameter("uid");
                if (!isNullOrEmpty(uid))
                    out.println(getUserInfoJSON(uid));
                else {
                    JSONArray json = new JSONArray();
                    for (String _uid : storageIo.listUsers())
                        json.put(getUserInfoJSON(_uid));
                    out.println(json);
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
            case "register": {
                String email = req.getParameter("email");
                String name = req.getParameter("name");
                String password = req.getParameter("password");
                if(isNullOrEmpty(email)){
                    out.print("账号不能为空");
                    return;
                }
                if(isNullOrEmpty(password)){
                    out.print("密码不能为空");
                    return;
                }
                
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
                    if(!isNullOrEmpty(name))
                        storageIo.setUserName(user.getUserId(), name);
                    out.print("OK");
                }
                else
                    out.print("此账号已被注册");
                break;
            }
            case "modify": {
                String uid = req.getParameter("uid");
                String name = req.getParameter("name");
                String oldPassword = req.getParameter("old");
                String newPassword = req.getParameter("new");
                if(isNullOrEmpty(uid)){
                    out.print("账号不能为空");
                    return;
                }
                if(isNullOrEmpty(name)){
                    out.print("显示名称不能为空");
                    return;
                }
                if(isNullOrEmpty(oldPassword)){
                    out.print("旧密码不能为空");
                    return;
                }
                if(newPassword == null)
                    newPassword = "";
                
                User user = storageIo.getUser(uid);
                String hash = user.getPassword();
                if ((hash == null) || hash.equals("")) {
                    if(newPassword.equals(""))
                        newPassword = "123456";
                    
                    String hashedPassword = "";
                    try {
                        hashedPassword = PasswordHash.createHash(newPassword);
                    } catch (Exception e) {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                    }
                    storageIo.setUserName(uid, name);
                    storageIo.setUserPassword(uid, hashedPassword);
                    out.print("OK");
                } else {
                    boolean validLogin = false;
                    try {
                        validLogin = PasswordHash.validatePassword(oldPassword, hash);
                    } catch (Exception e) {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                        return;
                    }
                    if (validLogin) {
                        if (!newPassword.equals("")) {
                            String hashedPassword = "";
                            try {
                                hashedPassword = PasswordHash.createHash(newPassword);
                            } catch (Exception e) {
                                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                                return;
                            }
                            storageIo.setUserPassword(uid, hashedPassword);
                        }
                        storageIo.setUserName(uid, name);
                        out.print("OK");
                    } else
                        out.print("旧密码错误");
                }
                break;
            }
        }
    }
    
    private JSONObject getUserInfoJSON(String uid) {
        User user = storageIo.getUser(uid);

        JSONObject json = new JSONObject();
        json.put("uid", uid);
        json.put("email", user.getUserEmail());
        json.put("name", user.getUserName());
        json.put("lastVisited", storageIo.getUserLastVisited(uid));

        JSONArray groups = new JSONArray();
        for (long gid : storageIo.getUserGroups(uid))
            groups.put(gid);
        json.put("groups", groups);

        return json;
    }
    
    private static boolean isNullOrEmpty(String str){
        return (str == null) || str.equals("");
    }
}
