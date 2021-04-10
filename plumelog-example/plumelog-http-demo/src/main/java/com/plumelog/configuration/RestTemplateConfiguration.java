package com.plumelog.configuration;

import com.plumelog.http.restTemplate.PlumelogRestTemplateInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YIJIUE
 */
@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restConfig() {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new PlumelogRestTemplateInterceptor());

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}
