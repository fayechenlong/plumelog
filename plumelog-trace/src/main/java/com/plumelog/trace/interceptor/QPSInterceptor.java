package com.plumelog.trace.interceptor;

import com.plumelog.trace.factory.QPSCalculatorHandlerFactory;
import com.plumelog.trace.handler.QPSCalculatorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class QPSInterceptor extends HandlerInterceptorAdapter {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 调用url
        try {
            String requestURI = request.getRequestURI();
            QPSCalculatorHandler qpsCalculatorHandler = QPSCalculatorHandlerFactory.getHandler(requestURI);
            qpsCalculatorHandler.record();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
