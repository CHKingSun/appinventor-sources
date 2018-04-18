package com.google.appinventor.server;

import com.google.appinventor.server.flags.Flag;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class LocalAccessFilter implements Filter {
    private static final Logger LOG = Logger.getLogger(OdeAuthFilter.class.getName());
    private static final Flag<Boolean> enabled = Flag.createFlag("EnableLocalAccessFilter", true);
    private Set<String> localAddresses = new HashSet<>();

    @Override
    public void init(FilterConfig config) {
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface networkInterfaces = e.nextElement();
                Enumeration<InetAddress> addresses = networkInterfaces.getInetAddresses();
                while (addresses.hasMoreElements())
                    localAddresses.add(addresses.nextElement().getHostAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (enabled.get()) {
            StringBuilder sb = new StringBuilder();
            sb.append("LocalAccessFilter is enabled, ");
            sb.append("/admin permits access from the following addresses:\n");
            for (String addr : localAddresses)
                sb.append(addr).append('\n');
            LOG.info(sb.toString());
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        if (enabled.get()) {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            HttpServletResponse httpResp = (HttpServletResponse) resp;
            LOG.info("[" + httpReq.getRequestURI() + "] Access from " + httpReq.getRemoteAddr());

            if (localAddresses.contains(httpReq.getRemoteAddr()))
                chain.doFilter(req, resp);
            else
                httpResp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else
            chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }
}