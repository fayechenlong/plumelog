package com.plumelog.trace.interceptor;

import com.plumelog.trace.handler.QPSCalculatorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class QPSInterceptor extends HandlerInterceptorAdapter {


    @Autowired
    private QPSCalculatorHandler qpsCalculatorHandler;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //todo 如何不影响业务 异步统计qps
        qpsCalculatorHandler.record();

        return true;
    }
}
