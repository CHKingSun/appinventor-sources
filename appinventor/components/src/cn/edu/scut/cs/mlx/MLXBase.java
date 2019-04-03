package cn.edu.scut.cs.mlx;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.*;
import com.google.appinventor.components.runtime.errors.*;

import android.app.Activity;
import android.util.Log;

import java.net.*;
import java.io.*;
import java.util.*;

public class MLXBase extends AndroidNonvisibleComponent implements Component{
    static String SERVER = "";
    private Activity activity = null;
    
    public MLXBase(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
    }
    
    protected Component getInstance(){
        return this;
    }
    
    protected String getExtensionName(){
        return getInstance().getClass().getName();
    }
    
    protected void raiseError(String functionName, int errorCode, String msg){
        Log.i("MLXBase", "raiseError");
        Log.i("MLXBase", "extensionName = " + getExtensionName());
        Log.i("MLXBase", "functionName = " + functionName);
        Log.i("MLXBase", "errorCode = " + errorCode);
        Log.i("MLXBase", "msg = " + msg);
        form.dispatchErrorOccurredEvent(getInstance(), functionName,
            ErrorMessages.ERROR_EXTENSION_ERROR, errorCode, getExtensionName(), msg);
    }
    
    protected void get(final String path, final AsyncCallbackPair<String> callback){
        Log.i("MLXBase", "GET " + SERVER + path);
        AsynchUtil.runAsynchronously(new Runnable(){
            public void run(){
                HttpURLConnection conn = null;
                try{
                    URL url = new URL(SERVER + path);
                    conn = (HttpURLConnection)url.openConnection();
                    conn.connect();
                    if(callback != null){
                        Log.i("MLXBase", "Server Response Code " + conn.getResponseCode());
                        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                            ByteArrayOutputStream buf = new ByteArrayOutputStream();
                            InputStream in = conn.getInputStream();
                            byte chunk[] = new byte[1024];
                            int len = in.read(chunk, 0, 1024);
                            while(len != -1){
                                buf.write(chunk, 0, len);
                                len = in.read(chunk, 0, 1024);
                            }

                            final String content = new String(buf.toByteArray(), "UTF-8");
                            MLXBase.this.activity.runOnUiThread(new Runnable(){
                                public void run(){
                                    callback.onSuccess(content);
                                }
                            });
                        }
                        else{
                            final String msg = "Server responded with code " + conn.getResponseCode();
                            raiseError("MLXBase.getRequest", conn.getResponseCode(), msg);
                            MLXBase.this.activity.runOnUiThread(new Runnable(){
                                public void run(){
                                    callback.onFailure(msg);
                                }
                            });
                        }
                    }
                }catch(Exception e){
                    raiseError("MLXBase.getRequest", -1, "Error while communicating with server, " + e.toString());
                }
                finally{
                    if(conn != null){
                        try{
                            conn.disconnect();
                        }catch(Exception e){}
                    }
                }
            }
        });
    }
    
    protected void post(final String path, final byte data[], final AsyncCallbackPair<String> callback){
        Log.i("MLXBase", "POST " + SERVER + path);
        AsynchUtil.runAsynchronously(new Runnable(){
            public void run(){
                HttpURLConnection conn = null;
                try{
                    URL url = new URL(SERVER + path);
                    conn = (HttpURLConnection)url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setFixedLengthStreamingMode(data.length);
                    conn.connect();
                    
                    OutputStream out = conn.getOutputStream();
                    out.write(data, 0, data.length);
                    out.flush();
                    out.close();
                    
                    if(callback != null){
                        Log.i("MLXBase", "Server Response Code " + conn.getResponseCode());
                        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                            ByteArrayOutputStream buf = new ByteArrayOutputStream();
                            InputStream in = conn.getInputStream();
                            byte chunk[] = new byte[1024];
                            int len = in.read(chunk, 0, 1024);
                            while(len != -1){
                                buf.write(chunk, 0, len);
                                len = in.read(chunk, 0, 1024);
                            }
                            
                            final String content = new String(buf.toByteArray(), "UTF-8");
                            MLXBase.this.activity.runOnUiThread(new Runnable(){
                                public void run(){
                                    callback.onSuccess(content);
                                }
                            });
                        }
                        else{
                            final String msg = "Server responded with code " + conn.getResponseCode();
                            raiseError("MLXBase.postRequest", conn.getResponseCode(), msg);
                            MLXBase.this.activity.runOnUiThread(new Runnable(){
                                public void run(){
                                    callback.onFailure(msg);
                                }
                            });
                        }
                    }
                }catch(Exception e){
                    raiseError("MLXBase.postRequest", -1, "Error while communicating with server, " + e.toString());
                }
                finally{
                    if(conn != null){
                        try{
                            conn.disconnect();
                        }catch(Exception e){}
                    }
                }
            }
        });
    }
    
    protected void uploadImage(String imageFilePath, AsyncCallbackPair<String> callback){
        byte data[] = readFile(imageFilePath);
        if(data.length >= 0)
            post("/images/upload", data, callback);
    }
    
    protected String urlEncode(String parameter){
        return URLEncoder.encode(parameter);
    }
    
    protected byte[] readFile(String file){
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        InputStream in = null;
        try{
            in = MediaUtil.openMedia(form, file);
            byte chunk[] = new byte[1024];
            int len = in.read(chunk, 0, 1024);
            while(len != -1){
                buf.write(chunk, 0, len);
                len = in.read(chunk, 0, 1024);
            }
        }catch(Exception e){
            raiseError("MLXBase.readFile", -1, "Cannot read file: " + file);
        }
        finally{
            if(in != null){
                try{
                    in.close();
                }catch(Exception e){}
            }
        }
        return buf.toByteArray();
    }
}