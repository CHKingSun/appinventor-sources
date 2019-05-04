package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

public class ProjectUploadServlet extends OdeServlet {
    private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
    private final FileExporter fileExporter = new FileExporterImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("text/html; charset=utf-8");

        String action = req.getParameter("action");
        if (action == null)
            action = "";
        switch (action) {
            case "testLogin": {
                String hostname = req.getParameter("host");
                int port = 80;
                if(req.getParameter("port") != null)
                    port = Integer.parseInt(req.getParameter("port"));
                String username = req.getParameter("username");
                String password = req.getParameter("password");
                if(isNullOrEmpty(hostname)||isNullOrEmpty(username)||isNullOrEmpty(password)){
                    resp.getWriter().println(false);
                    return;
                }

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
                            resp.getWriter().println(c.getValue());
                        }
                }catch(Exception e){
                    e.printStackTrace();
                }
                resp.getWriter().println(false);
                break;
            }
            case "upload": {
                String hostname = req.getParameter("host");
                int port = 80;
                if(req.getParameter("port") != null)
                    port = Integer.parseInt(req.getParameter("port"));
                String cookie = req.getParameter("cookie");
                String uid = req.getParameter("uid");
                long pid = Long.parseLong(req.getParameter("pid"));
                String name = req.getParameter("name");
                if(isNullOrEmpty(hostname)||isNullOrEmpty(cookie)||isNullOrEmpty(uid)){
                    resp.getWriter().println(-1);
                    return;
                }
                if(isNullOrEmpty(name))
                    name = storageIo.getProjectName(uid, pid);

                int statusCode = 200;
                try {
                    ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(uid, pid, false, false, null, false, false, false, false);
                    RawFile srcFile = zipFile.getRawFile();
                    byte[] content = srcFile.getContent();

                    CloseableHttpClient client = HttpClientBuilder.create().build();
                    HttpHost host = new HttpHost(hostname, port);
                    HttpPost postReq = new HttpPost("/ode/upload/project/" + name);
                    postReq.setHeader("Cookie", "AppInventor=" + cookie);
                    HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody("uploadProjectArchive", content).build();
                    postReq.setEntity(entity);

                    HttpResponse postResp = client.execute(host, postReq);
                    statusCode = postResp.getStatusLine().getStatusCode();
                    client.close();
                }catch(Exception e){
                    e.printStackTrace();
                    resp.getWriter().println(-2);
                    return;
                }
                resp.getWriter().println(statusCode);
                break;
            }
            default:
        }
    }

    private boolean isNullOrEmpty(String s){
        return (s == null) || s.equals("");
    }
}
