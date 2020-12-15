package com.plumelog.trace.config;

import com.plumelog.trace.handler.QPSCalculatorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QPSCalculatorConfig {


    @Bean
    public QPSCalculatorHandler qpsCalculatorHandler(){
        return new QPSCalculatorHandler();
    }

}
