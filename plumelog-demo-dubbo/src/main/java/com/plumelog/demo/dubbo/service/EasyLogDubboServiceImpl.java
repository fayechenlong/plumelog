package com.plumelog.demo.dubbo.service;

import com.plumelog.trace.annotation.Trace;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Service
@Component
public class EasyLogDubboServiceImpl implements EasyLogDubboService{

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(EasyLogDubboServiceImpl.class);

    @Trace
    @Override
    public void testLogDubbo(){
        logger.info("I am EasyLogDubboService testLogDubbo");
    }

}
