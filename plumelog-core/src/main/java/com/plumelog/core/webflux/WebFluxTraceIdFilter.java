package com.plumelog.core.webflux;

import com.plumelog.core.TraceId;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author YIJIUE
 */
public class WebFluxTraceIdFilter implements WebFilter {

    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, @NonNull WebFilterChain webFilterChain) {

        List<String> traceIds = serverWebExchange.getRequest().getHeaders().get("traceId");
        if (!CollectionUtils.isEmpty(traceIds)) {
            TraceId.logTraceID.set(traceIds.get(0));
        } else {
            TraceId.set();
        }

        return webFilterChain.filter(serverWebExchange);
    }
}
