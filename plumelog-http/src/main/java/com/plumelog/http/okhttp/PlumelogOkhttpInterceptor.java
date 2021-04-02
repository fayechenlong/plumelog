package com.plumelog.http.okhttp;

import com.plumelog.core.TraceId;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.plumelog.core.constant.LogMessageConstant.TRACE_ID;

/**
 * @author YIJIUE
 */
public class PlumelogOkhttpInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        String traceId = TraceId.getTraceId(TraceId.logTraceID.get());
        Request request = chain.request().newBuilder()
                .addHeader(TRACE_ID, traceId)
                .build();
        return chain.proceed(request);
    }
}
