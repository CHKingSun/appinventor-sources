package com.google.appinventor.server;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.logging.Logger;

public class LocalAccessFilter implements Filter{
    private static final Logger LOG = Logger.getLogger(OdeAuthFilter.class.getName());
    private Set<String> localAddresses = new HashSet<>();
    
    @Override
    public void init(FilterConfig config){
        try{
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements()){
                NetworkInterface networkInterfaces = e.nextElement();
                Enumeration<InetAddress> addresses = networkInterfaces.getInetAddresses();
                while(addresses.hasMoreElements())
                    localAddresses.add(addresses.nextElement().getHostAddress());
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        LOG.info("LocalAddresses:");
        for(String addr : localAddresses)
            LOG.info(addr);
    }
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest)req;
        HttpServletResponse httpResp = (HttpServletResponse)resp;
        LOG.info("[" + httpReq.getRequestURI() + "] Access from " + httpReq.getRemoteAddr());
        
        if(localAddresses.contains(httpReq.getRemoteAddr()))
            chain.doFilter(req, resp);
        else{
            httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    }
    
    @Override
    public void destroy(){}
}