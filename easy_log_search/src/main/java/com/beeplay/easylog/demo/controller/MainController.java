package com.beeplay.easylog.demo.controller;


import com.beeplay.easylog.core.client.EasyLogger;
import com.beeplay.easylog.demo.service.MainService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class MainController {
    private static EasyLogger logger = EasyLogger.getLogger(Logger.getLogger(MainController.class));
    @Autowired
    private MainService mainService;
    @RequestMapping("/index")
    private String index(String data) {
        logger.info("I am MainController");
        logger.info(data);
        mainService.testLog();
        return "index";
    }
}


