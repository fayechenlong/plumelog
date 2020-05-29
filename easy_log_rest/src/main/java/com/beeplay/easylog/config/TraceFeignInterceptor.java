package com.beeplay.easylog.config;

import com.beeplay.easylog.core.TraceId;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * className：TraceFeignInterceptor
 * description： TODO
 * time：2020-05-29.15:44
 *
 * @author Tank
 * @version 1.0.0
 */
@Component
@Slf4j
public class TraceFeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String traceId = TraceId.logTraceID.get();
        log.info("【TraceFeignInterceptor】"+traceId);
        requestTemplate.header("TraceId",traceId);
    }
}
