package com.beeplay.easylog;
import com.beeplay.easylog.core.client.TransId;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;


@Component
public class Interceptor extends HandlerInterceptorAdapter{

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        TransId.logTranID.set(UUID.randomUUID().toString());

        return true;
    }
}
