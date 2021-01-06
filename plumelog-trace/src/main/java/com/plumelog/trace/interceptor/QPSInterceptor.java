package com.plumelog.trace.interceptor;

import com.plumelog.trace.factory.QPSCalculatorHandlerFactory;
import com.plumelog.trace.handler.QPSCalculatorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class QPSInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<StopWatch> stopWatchThreadLocal = new NamedThreadLocal<>("ConsumeTime-StopWatch");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            StopWatch stopWatch = new StopWatch("plumelog");
            stopWatchThreadLocal.set(stopWatch);
            stopWatch.start("qps");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        try {
            String requestURI = request.getRequestURI();

            StopWatch stopWatch = stopWatchThreadLocal.get();
            stopWatch.stop();
            long totalTimeMillis = stopWatch.getTotalTimeMillis();
            stopWatchThreadLocal.remove();

            QPSCalculatorHandler qpsCalculatorHandler = QPSCalculatorHandlerFactory.getHandler(requestURI);
            qpsCalculatorHandler.record(totalTimeMillis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
