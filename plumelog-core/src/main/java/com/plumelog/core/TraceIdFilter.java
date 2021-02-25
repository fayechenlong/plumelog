package com.plumelog.core;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * className：TraceIdFilter
 * description： TODO
 * time：2020-06-05.14:30
 *
 * @author Tank
 * @version 1.0.0
 */
public class TraceIdFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            TraceId.set();
        } finally {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
    @Override
    public void destroy() {
    }
}
