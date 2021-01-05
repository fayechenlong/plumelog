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

    private NamedThreadLocal<StopWatch> stopWatchThreadLocal = new NamedThreadLocal<StopWatch>("ConsumeTime-StopWatch");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 调用url
        try {
            String requestURI = request.getRequestURI();
            QPSCalculatorHandler qpsCalculatorHandler = QPSCalculatorHandlerFactory.getHandler(requestURI);
            qpsCalculatorHandler.record();

            StopWatch stopWatch = new StopWatch(requestURI);
            stopWatchThreadLocal.set(stopWatch);
            stopWatch.start(requestURI);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        String requestURI = request.getRequestURI();
        final StopWatch stopWatch = stopWatchThreadLocal.get();
        stopWatch.stop();
        long totalTimeMillis =  stopWatch.getTotalTimeMillis();

        stopWatchThreadLocal.remove();
    }


}
