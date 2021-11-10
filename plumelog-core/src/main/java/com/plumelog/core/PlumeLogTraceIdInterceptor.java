package com.plumelog.core;

import com.plumelog.core.util.IdWorker;
import com.plumelog.core.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class PlumeLogTraceIdInterceptor extends HandlerInterceptorAdapter{

    IdWorker worker = new IdWorker(1,1,1);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceId = request.getHeader("traceId");
        if (StringUtils.isEmpty(traceId)){
            IdWorker worker = new IdWorker(1,1,1);
            TraceId.logTraceID.set(String.valueOf(worker.nextId()));
        }else{
            TraceId.logTraceID.set(traceId);
        }
        return true;
    }

}
