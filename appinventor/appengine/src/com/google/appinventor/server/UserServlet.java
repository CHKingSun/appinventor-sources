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
			case "find":{
				String email = req.getParameter("email");
				if(email != null){
					String emailLower = email.toLowerCase();
					boolean found = false;
					for(AdminUser user : storageIo.searchUsers(""))
						if(email.equals(user.getEmail()) || emailLower.equals(user.getEmail())){
							found = true;
							break;
						}
					out.println(found ? "YES" : "NO");
				}
				break;
			}
			case "register":{
				out.println("<html>");
				out.println("<head>");
				out.println("<meta charset=\"utf8\">");
				out.println("<title>注册新账号</title>");
				out.println("</head>");
				out.println("<body>");
				out.println("<center>");
				out.println("<p><h1>注册新账号</h1></p>");
				out.println("<form action=\"/api/user/?action=register\" method=\"POST\">");
				out.println("<p>账号: <input type=\"text\" name=\"email\"></p>");
				out.println("<p>密码: <input type=\"password\" name=\"password\"></p>");
				out.println("<p><input type=\"submit\" style=\"font-size: 300%;\"></p>");
				out.println("</form>");
				out.println("<a href=\"#\" onclick=\"window.history.back();\">返回</a>");
				out.println("</center>");
				out.println("</body>");
				out.println("</html>");
				break;
			}
			case "modify":{
				String uid = req.getParameter("uid");
				if(uid != null){
					User user = storageIo.getUser(uid);
					out.println("<html>");
					out.println("<head>");
					out.println("<meta charset=\"utf8\">");
					out.println("<title>修改账号信息</title>");
					out.println("</head>");
					out.println("<body>");
					out.println("<center>");
					out.println("<p><h1>修改账号信息</h1></p>");
					out.println("<form action=\"/api/user/?action=modify\" method=\"POST\">");
					out.println("<input type=\"hidden\" name=\"uid\" value=\"" + uid + "\"></p>");
					out.println("<p>账号: " + user.getUserEmail() + "</p>");
					out.println("<p>显示名称: <input type=\"text\" name=\"name\" value=\"" + user.getUserName() + "\"></p>");
					out.println("<p>旧密码: <input type=\"password\" name=\"old\"></p>");
					out.println("<p>新密码: <input type=\"password\" name=\"new\"></p>");
					out.println("<p><input type=\"submit\" style=\"font-size: 300%;\"></p>");
					out.println("</form>");
					out.println("<a href=\"#\" onclick=\"window.history.back();\">返回</a>");
					out.println("</center>");
					out.println("</body>");
					out.println("</html>");
				}
				break;
			}
			default:{
				JSONArray json = new JSONArray();
				for(AdminUser user : storageIo.searchUsers("")){
					JSONObject obj = new JSONObject();
					obj.put("uid", user.getId());
					obj.put("email", user.getEmail());
					obj.put("name", user.getName());
					json.put(obj);
				}
				out.println(json);
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
					else{
						out.println("<b>此账号已被注册!</b> 正在跳转到注册页面...");
						resp.setHeader("refresh", "3;url=/api/user?action=register");
					}
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
						else
							out.println("<b>旧密码错误!</b>");
					}
				}
				resp.setHeader("refresh", "3;url=/?locale=zh_CN");
				break;
			}
			case "setPassword":{
				String uid = req.getParameter("uid");
				String password = req.getParameter("password");
				if(password == null)
					password = "";
				if(uid != null){
					User user = storageIo.getUser(uid);
					String hashedPassword = "";
					try {
						hashedPassword = PasswordHash.createHash(password);
					} catch (Exception e) {
						resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
					}
					storageIo.setUserPassword(uid, hashedPassword);
				}
				break;
			}
		}
	}
}
