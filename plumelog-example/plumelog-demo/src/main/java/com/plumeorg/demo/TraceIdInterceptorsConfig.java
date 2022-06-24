package com.plumeorg.demo;

import com.plumelog.core.PlumeLogTraceIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Configuration
public class TraceIdInterceptorsConfig extends WebMvcConfigurerAdapter{
//    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {"classpath:/META-INF/resources/", "classpath:/resources/", "classpath:/static/", "classpath:/public/"};
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
//    }
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//       // registry.addInterceptor(new PlumeLogTraceIdInterceptor());
//        registry.addInterceptor(new PlumeLogTraceIdInterceptor());
//        super.addInterceptors(registry);
//    }

}
