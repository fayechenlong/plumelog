package com.plumelog.controller;

import com.plumelog.core.TraceId;
import okhttp3.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author YIJIUE
 */
@RestController
public class TestController {

    @Resource
    private OkHttpClient okHttpClient;

    @Resource
    private RestTemplate restTemplate;

    @GetMapping("/okhttp")
    public String okhttp() throws IOException {
        String url = "http://wwww.baidu.com";
        final Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).execute();
        System.out.println(TraceId.logTraceID.get());
        return TraceId.logTraceID.get();
    }

    @GetMapping("/rest")
    public String rest() {
        restTemplate.getForObject("http://www.baiduxxx.com", String.class);
        return TraceId.logTraceID.get();
    }
}
