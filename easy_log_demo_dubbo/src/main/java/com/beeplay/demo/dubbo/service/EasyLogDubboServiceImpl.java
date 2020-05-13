package com.beeplay.demo.dubbo.service;

import org.apache.dubbo.config.annotation.Service;
import org.slf4j.LoggerFactory;

/**
 * @ClassName EasyLogDubboService
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/13 16:02
 * @Version 1.0
 **/
@Service
public class EasyLogDubboServiceImpl implements EasyLogDubboService{
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(EasyLogDubboServiceImpl.class);

    @Override
    public void testLogDubbo(){
        logger.info("I am EasyLogDubboService testLogDubbo");
    }

}
