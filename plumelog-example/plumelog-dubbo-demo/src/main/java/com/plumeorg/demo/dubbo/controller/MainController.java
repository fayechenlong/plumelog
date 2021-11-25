package com.plumeorg.demo.dubbo.controller;


import com.plumeorg.demo.dubbo.service.EasyLogDubboService;
import com.plumelog.trace.annotation.Trace;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MainController {
    private static org.slf4j.Logger logger= LoggerFactory.getLogger(MainController.class);
    @Autowired
    private EasyLogDubboService easyLogDubboService;


    @RequestMapping("/index")
    @Trace
    public String index(String data) {
        logger.info("I am MainController");
        logger.info(data);
        easyLogDubboService.testLogDubbo();
        return "index";
    }
}


