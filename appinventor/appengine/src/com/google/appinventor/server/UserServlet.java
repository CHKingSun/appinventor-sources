package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.admin.AdminUser;
import com.google.appinventor.shared.rpc.user.User;
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
		resp.setContentType("text/html; charset=utf-8");
		PrintWriter out = resp.getWriter();
		
		String action = req.getParameter("action");
		if(action == null)
			action = "";
		switch(action){
            case "groups":{
                JSONArray json = new JSONArray();
                for(long gid : storageIo.getGroups()){
                    JSONObject obj = new JSONObject();
                    obj.put("gid", gid);
                    obj.put("name", storageIo.getGroupName(gid));
                    json.put(obj);
                }
                out.println(json);
                break;
            }
            case "groupUsers":{
                long gid = Long.parseLong(req.getParameter("gid"));
                JSONArray json = new JSONArray();
                for(String uid : storageIo.getGroupUsers(gid))
                    json.put(getUserInfoJSON(uid));
                out.println(json);
                break;
            }
			default:{
                String uid = req.getParameter("uid");
                if(uid != null)
                    out.println(getUserInfoJSON(uid));
                else{
                    JSONArray json = new JSONArray();
                    for(AdminUser user : storageIo.searchUsers(""))
                        json.put(getUserInfoJSON(user.getId()));
                    out.println(json);
                }
				break;
			}
		}
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html; charset=utf-8");
		PrintWriter out = resp.getWriter();
		
		String action = req.getParameter("action");
		if(action == null)
			action = "";
		switch(action){
			case "register":{
				String email = req.getParameter("email");
				String password = req.getParameter("password");
				if((email != null) && (password != null)){
					User user = storageIo.getUserFromEmail(email);
					String hash = user.getPassword();
					if ((hash == null) || hash.equals("")) {
						String hashedPassword = "";
						try {
							hashedPassword = PasswordHash.createHash(password);
						} catch (Exception e) {
							resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
						}
						storageIo.setUserPassword(user.getUserId(), hashedPassword);
						out.println("<b>注册成功!</b> 正在跳转到登录页面...");
						resp.setHeader("refresh", "3;url=/login");
					}
					else
						resp.setHeader("refresh", "3;url=/register.jsp?error=" + URLEncoder.encode("此账号已被注册", "UTF-8"));
				}
				break;
			}
			case "modify":{
				String uid = req.getParameter("uid");
				if(uid != null){
					User user = storageIo.getUser(uid);
					String name = req.getParameter("name");
					String oldPassword = req.getParameter("old");
					String newPassword = req.getParameter("new");
					String hash = user.getPassword();
					
					if ((hash == null) || hash.equals("")) {
						String hashedPassword = "";
						try {
							hashedPassword = PasswordHash.createHash(newPassword);
						} catch (Exception e) {
							resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
						}
						storageIo.setUserName(uid, name);
						storageIo.setUserPassword(uid, hashedPassword);
						out.println("<b>账号信息修改成功!</b>");
					}
					else{
						boolean validLogin = false;
						try {
							validLogin = PasswordHash.validatePassword(oldPassword, hash);
						} catch (Exception e) {
							resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
						}
						if(validLogin){
							if(!newPassword.equals("")){
								String hashedPassword = "";
								try {
									hashedPassword = PasswordHash.createHash(newPassword);
								} catch (Exception e) {
									resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
								}
								storageIo.setUserPassword(uid, hashedPassword);
							}
							storageIo.setUserName(uid, name);
							out.println("<b>账号信息修改成功!</b>");
						}
						else{
                            String params = "?uid=" + uid;
                            params += "&email=" + user.getUserEmail();
                            params += "&name=" + user.getUserName();
                            params += "&error=旧密码错误";
                            resp.setHeader("refresh", "3;url=/modify.jsp" + URLEncoder.encode(params, "UTF-8"));
                            return;
                        }
					}
				}
				resp.setHeader("refresh", "3;url=/");
				break;
			}
		}
	}
    
    private JSONObject getUserInfoJSON(String uid){
        User user = storageIo.getUser(uid);
        
        JSONObject json = new JSONObject();
        json.put("uid", uid);
        json.put("email", user.getUserEmail());
        json.put("name", user.getUserName());
        
        JSONArray groups = new JSONArray();
        for(long gid : storageIo.getUserGroups(uid))
            groups.put(gid);
        json.put("groups", groups);
        
        return json;
    }
}
