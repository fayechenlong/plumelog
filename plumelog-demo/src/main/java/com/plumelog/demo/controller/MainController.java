package com.plumelog.demo.controller;

import com.plumelog.core.TraceId;
import com.plumelog.core.TraceIdGenerator;
import com.plumelog.demo.service.MainService;
import com.plumelog.trace.annotation.Trace;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    private static org.slf4j.Logger logger= LoggerFactory.getLogger(MainController.class);
    @Autowired
    private MainService mainService;

    @RequestMapping("/index")
    @Trace
    public String index(String data) {
        logger.info("I am MainController" + System.getProperty("user.home"));
        if(data==null||"".equals(data)){
            data="你啥都没有输入！";
        }
        logger.info("你输入的是{}",data);
        mainService.testLog(data);
        return data;
    }

    @RequestMapping("/mdc")
    @Trace
    public String dmc(String data) {
        logger.info("I am MainController" + System.getProperty("user.home"));
        //这里的key Context在plumelog的ui的【扩展字段】；然后在【日志查询】下方的【显示字段】选择你配置的拓展字段
        MDC.put("Context", TraceIdGenerator.generate());
        if(data==null||"".equals(data)){
            data="你啥都没有输入！";
        }
        logger.info("你输入的是{}",data);
        mainService.testLog(data);
        return data;
    }
}


