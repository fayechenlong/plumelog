package com.plumelog;

import com.plumelog.core.LogMessageThreadLocal;
import com.plumelog.core.TraceId;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@EnableDubbo
@ServletComponentScan
public class AppStart {
    public static void main( String[] args ){
        SpringApplication.run(AppStart.class, args);
        LogMessageThreadLocal.logMessageThreadLocal.remove();
        TraceId.logTraceID.remove();
    }
}
