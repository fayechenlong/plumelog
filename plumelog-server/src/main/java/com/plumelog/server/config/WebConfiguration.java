package com.plumelog.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Bean
    public FilterRegistrationBean CorsFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new CorsFilter());
        List<String> urlList = new ArrayList();
        urlList.add("/*");
        registration.setUrlPatterns(urlList);
        registration.setName("CorsFilter");
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public LoginCheckInterceptor loginCheckInterceptor() {
        return new LoginCheckInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePath = new ArrayList<>();
        excludePath.add("/login");
        excludePath.add("/plumelogServer/login");
        excludePath.add("/logout");
        excludePath.add("/plumelogServer/logout");
        registry.addInterceptor(loginCheckInterceptor()).addPathPatterns("/**")
                .excludePathPatterns(excludePath);

    }
}
