package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.entity.*;
import org.apache.http.impl.client.*;
import org.apache.http.cookie.*;
import org.apache.http.impl.cookie.*;
import org.apache.http.message.*;
import org.apache.http.entity.mime.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.json.JSONObject;

public class RemoteUploadServlet extends HttpServlet {
    private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
    private final FileExporter fileExporter = new FileExporterImpl();

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("text/html; charset=utf-8");
        PrintWriter out = resp.getWriter();

        String action = req.getParameter("action");
        if (action == null)
            action = "";
        switch (action) {
            case "testLogin":{
                String hostname = req.getParameter("host");
                int port = 80;
                if(req.getParameter("port") != null)
                    port = Integer.parseInt(req.getParameter("port"));
                String username = req.getParameter("username");
                String password = req.getParameter("password");
                if(isNullOrEmpty(hostname)||isNullOrEmpty(username)||isNullOrEmpty(password)){
                    out.print("NO");
                    return;
                }
                
                boolean success = false;
                CookieStore cookiestore = new BasicCookieStore();
                try(CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookiestore).build()){
                    HttpHost host = new HttpHost(hostname, port);
                    HttpPost postReq = new HttpPost("/login");
                    
                    List<BasicNameValuePair> params = new LinkedList<>();
                    params.add(new BasicNameValuePair("email", username));
                    params.add(new BasicNameValuePair("password", password));
                    postReq.setEntity(new UrlEncodedFormEntity(params));
                    
                    client.execute(host, postReq);
                    for(Cookie c : cookiestore.getCookies())
                        if(c.getName().equals("AppInventor")){
                            out.print(c.getValue());
                            success = true;
                            break;
                        }
                }catch(Exception e){
                    e.printStackTrace();
                }
                
                if(!success)
                    out.print("NO");
                break;
            }
            default:{
                String hostname = req.getParameter("host");
                int port = 80;
                if(req.getParameter("port") != null)
                    port = Integer.parseInt(req.getParameter("port"));
                String cookie = req.getParameter("cookie");
                String uid = req.getParameter("uid");
                long pid = Long.parseLong(req.getParameter("pid"));
                String name = req.getParameter("name");
                if(isNullOrEmpty(hostname)||isNullOrEmpty(cookie)||isNullOrEmpty(uid)){
                    out.print("NO");
                    return;
                }
                if(isNullOrEmpty(name))
                    name = storageIo.getProjectName(uid, pid);
                
                boolean success = false;
                try {
                    ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(uid, pid, false, false, null, false, false, false, false);
                    RawFile srcFile = zipFile.getRawFile();
                    byte content[] = srcFile.getContent();
                    
                    CloseableHttpClient client = HttpClientBuilder.create().build();
                    HttpHost host = new HttpHost(hostname, port);
                    HttpPost postReq = new HttpPost("/ode/upload/project/" + name);
                    postReq.setHeader("Cookie", "AppInventor=" + cookie);
                    HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody("uploadProjectArchive", content).build();
                    postReq.setEntity(entity);
                    
                    HttpResponse postResp = client.execute(host, postReq);
                    success = postResp.getStatusLine().getStatusCode() == 200;
                    client.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
                
                JSONObject json = new JSONObject();
                json.put("status", success ? "OK" : "NO");
                json.put("name", name);
                out.print(json);
                break;
            }
        }
    }
    
    private boolean isNullOrEmpty(String s){
        return (s == null) || s.equals("");
    }
}