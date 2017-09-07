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

public class AdminIPFilter implements Filter{
    @Override
    public void init(FilterConfig config){}
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        String ip = req.getRemoteAddr();
        if(ip.equals("127.0.0.1"))
            chain.doFilter(req, resp);
        else{
            ((HttpServletResponse)resp).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
    }
    
    @Override
    public void destroy(){}
}