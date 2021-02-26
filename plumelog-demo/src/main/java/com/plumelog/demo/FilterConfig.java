package com.plumelog.demo;

import com.plumelog.core.TraceIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * className：FilterConfig
 * description： TODO
 * time：2020-06-05.15:36
 *
 * @author Tank
 * @version 1.0.0
 */
@Configuration
public class FilterConfig {


//    @Bean
//    public FilterRegistrationBean filterRegistrationBean1() {
//        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
//        filterRegistrationBean.setFilter(initCustomFilter());
//        filterRegistrationBean.addUrlPatterns("/*");
//        filterRegistrationBean.setOrder(Integer.MIN_VALUE);
//        return filterRegistrationBean;
//    }
//
//    @Bean
//    public Filter initCustomFilter() {
//        return new TraceIdFilter();
//    }
}
