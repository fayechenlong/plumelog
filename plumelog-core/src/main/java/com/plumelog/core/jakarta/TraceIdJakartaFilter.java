package com.plumelog.core.jakarta;

import com.plumelog.core.TraceId;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class TraceIdJakartaFilter implements Filter {
    public TraceIdJakartaFilter() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest)servletRequest;
            String traceId = request.getParameter("traceId");
            if (traceId != null && !"".equals(traceId)) {
                TraceId.logTraceID.set(traceId);
            } else {
                TraceId.set();
            }
        } finally {
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    public void destroy() {
    }
}
