package com.plumelog.trace.config;

import com.plumelog.trace.interceptor.QPSInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(qpsInterceptor()).addPathPatterns("/**");
    }

    @Bean
    public QPSInterceptor qpsInterceptor() {
        return new QPSInterceptor();
    }


}
