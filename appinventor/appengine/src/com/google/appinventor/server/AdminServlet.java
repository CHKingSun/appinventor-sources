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

public class AdminServlet extends HttpServlet {
	private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html; charset=utf-8");
		PrintWriter out = resp.getWriter();
        
        String action = req.getParameter("action");
        if(action == null)
            action = "";
        switch(action){
            case "passwordReset":{
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
                    out.println("OK");
				}
				break;
			}
            case "removeUsers":{
                String users = req.getParameter("users");
                if(users != null){
                    JSONArray json = new JSONArray(users);
                    for(int i=0;i<json.length();i++){
                        String uid = json.getString(i);
                        storageIo.removeUser(uid);
                        for(long gid : storageIo.getGroups())
                            storageIo.removeUsersFromGroup(gid, Arrays.asList(uid));
                    }
                    out.println("OK");
                }
                break;
            }
            case "createGroup":{
                String name = req.getParameter("name");
                if(name != null){
                    storageIo.createGroup(name);
                    out.println("OK");
                }
                break;
            }
            case "removeGroup":{
                long gid = Long.parseLong(req.getParameter("gid"));
                storageIo.removeGroup(gid);
                out.println("OK");
                break;
            }
            case "addUsersToGroup":{
                long gid = Long.parseLong(req.getParameter("gid"));
                String users = req.getParameter("users");
                if(users != null){
                    JSONArray json = new JSONArray(users);
                    ArrayList<String> list = new ArrayList<String>();
                    for(int i=0;i<json.length();i++)
                        list.add(json.getString(i));
                    storageIo.addUsersToGroup(gid, list);
                    out.println("OK");
                }
                break;
            }
            case "removeUsersFromGroup":{
                long gid = Long.parseLong(req.getParameter("gid"));
                String users = req.getParameter("users");
                if(users != null){
                    JSONArray json = new JSONArray(users);
                    ArrayList<String> list = new ArrayList<String>();
                    for(int i=0;i<json.length();i++)
                        list.add(json.getString(i));
                    storageIo.removeUsersFromGroup(gid, list);
                    out.println("OK");
                }
                break;
            }
        }
    }
}
