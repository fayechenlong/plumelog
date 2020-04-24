package com.beeplay.easylog.demo.service;

import com.beeplay.easylog.core.client.EasyLogger;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MainService {
    private static EasyLogger logger = EasyLogger.getLogger(Logger.getLogger(MainService.class));

    public void testLog(){
        logger.info("I am service");
    }
}
