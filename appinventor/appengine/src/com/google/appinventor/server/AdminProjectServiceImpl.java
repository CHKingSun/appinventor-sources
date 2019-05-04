package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.AdminProjectService;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.UserProject;
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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The implementation of the RPC service which runs on the server.
 *
 * <p>Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 */
public class AdminProjectServiceImpl extends OdeRemoteServiceServlet implements AdminProjectService {
    private static final Logger LOG = Logger.getLogger(ProjectServiceImpl.class.getName());

    private final transient StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

    private final transient FileExporter fileExporter = new FileExporterImpl();

    @Override
    public String testLogin(String host, int port, String userName, String passwd) {
        host = host.trim();
        if (host.startsWith("http://")) {
            host = host.substring(7);
        } else if (host.startsWith("https://")) {
            host = host.substring(8);
        }
        CookieStore cookiestore = new BasicCookieStore();
        try(CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookiestore).build()){
            HttpHost httpHost = new HttpHost(host, port);
            HttpPost postReq = new HttpPost("/login");

            List<BasicNameValuePair> params = new LinkedList<>();
            params.add(new BasicNameValuePair("email", userName));
            params.add(new BasicNameValuePair("password", passwd));
            postReq.setEntity(new UrlEncodedFormEntity(params));

            client.execute(httpHost, postReq);
            for(Cookie c : cookiestore.getCookies()) {
                if(c.getName().equals("AppInventor")){
                    return c.getValue();
                }
            }
            return "";
        }catch(Exception e){
            LOG.log(Level.SEVERE, "Network error.", e);
            return "error";
        }
    }

    @Override
    public int uploadProject(long projectId, String host, int port, String cookie) {
        try {
            ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(userInfoProvider.getUserId(),
                    projectId, false, false,
                    null, false, false, false, false);
            RawFile srcFile = zipFile.getRawFile();
            byte[] content = srcFile.getContent();

            host = host.trim();
            if (host.startsWith("http://")) {
                host = host.substring(7);
            } else if (host.startsWith("https://")) {
                host = host.substring(8);
            }
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpHost httpHost = new HttpHost(host, port);
            HttpPost postReq = new HttpPost("/ode/upload/project/" + storageIo.getProjectName(
                    userInfoProvider.getUserId(), projectId
            ));
            postReq.setHeader("Cookie", "AppInventor=" + cookie);
            HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody("uploadProjectArchive", content).build();
            postReq.setEntity(entity);

            HttpResponse postResp = client.execute(httpHost, postReq);
            int statusCode = postResp.getStatusLine().getStatusCode();
            client.close();
            return statusCode;
        }catch(Exception e){
            LOG.log(Level.SEVERE, "Network error", e);
            return -1;
        }
    }
}
