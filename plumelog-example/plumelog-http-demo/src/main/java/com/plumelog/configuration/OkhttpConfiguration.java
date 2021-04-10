package com.plumelog.configuration;

import com.plumelog.http.okhttp.PlumelogOkhttpInterceptor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author YIJIUE
 */
@Configuration
public class OkhttpConfiguration {

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new PlumelogOkhttpInterceptor()).build();
    }
}
