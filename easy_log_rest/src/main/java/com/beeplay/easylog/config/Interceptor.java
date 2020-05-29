package com.beeplay.easylog.config;
import com.beeplay.easylog.core.TraceId;
import com.beeplay.easylog.core.util.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
@Slf4j
public class Interceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceId = request.getHeader("TraceId");
        if (StringUtils.isEmpty(traceId)){
            IdWorker worker = new IdWorker(1,1,1);
            TraceId.logTraceID.set(String.valueOf(worker.nextId()));
        }else{
            TraceId.logTraceID.set(traceId);
        }
        log.info("[Interceptor]  "+TraceId.logTraceID.get());
        return true;
    }
}
