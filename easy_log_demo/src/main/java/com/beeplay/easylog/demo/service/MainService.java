package com.beeplay.easylog.demo.service;


import com.beeplay.easylog.core.util.LogExceptionStackTrace;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MainService {
    private static org.slf4j.Logger logger= LoggerFactory.getLogger(MainService.class);
    public void testLog(){
        logger.info("I am service");


        String a=null;
        try {
             a.equals("111");
        }catch (Exception e){
            logger.error("{}", LogExceptionStackTrace.erroStackTrace(e));
        }

    }
}
