package com.beeplay.easylog.demo.controller;


import com.beeplay.easylog.demo.service.MainService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class MainController {
    private static org.slf4j.Logger logger= LoggerFactory.getLogger(MainController.class);
    @Autowired
    private MainService mainService;

    @RequestMapping("/index")
    public String index(String data) {
        logger.info("I am MainController");
        if(data!=null&&!"".equals(data)){
            data="你啥都没有输入！";
        }
        logger.info("你输入的是{}",data);
        mainService.testLog(data);
        return data;
    }
}


