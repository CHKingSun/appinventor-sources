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

public class LocalAccessFilter implements Filter{
    private Set<String> localAddresses = new HashSet<>();
    
    @Override
    public void init(FilterConfig config){
        try{
            localAddresses.add(InetAddress.getLocalHost().getHostAddress());
            for(InetAddress addr : InetAddress.getAllByName("localhost"))
                localAddresses.add(addr.getHostAddress());
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest)req;
        HttpServletResponse httpResp = (HttpServletResponse)resp;
        System.err.printf("[%s] Access from %s\n", httpReq.getRequestURI(), httpReq.getRemoteAddr());
        
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