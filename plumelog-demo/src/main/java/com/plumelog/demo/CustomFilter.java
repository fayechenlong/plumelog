package com.plumelog.demo;

import com.plumelog.core.TraceId;
import com.plumelog.core.lang.PlumeShutdownHook;
import com.plumelog.core.util.IdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * className：CustomFilter
 * description： TODO
 * time：2020-06-05.14:30
 *
 * @author Tank
 * @version 1.0.0
 */

//@WebFilter(filterName = "customFilter",urlPatterns = "/*")
public class CustomFilter implements Filter {
    Logger logger = LoggerFactory.getLogger(CustomFilter.class);

    private IdWorker worker = new IdWorker(1, 1, 1);
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        logger.info("doFilter ==========" + TraceId.logTraceID.get());
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String traceId = request.getHeader("TraceId");
            if (StringUtils.isEmpty(traceId)) {
                TraceId.logTraceID.set(String.valueOf(worker.nextId()));
            } else {
                TraceId.logTraceID.set(traceId);
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            logger.info("finally doFilter ==========" + TraceId.logTraceID.get());
        }
    }

    @Override
    public void destroy() {
        logger.info("destroy ============");
        PlumeShutdownHook.destroyAll();
    }
}
