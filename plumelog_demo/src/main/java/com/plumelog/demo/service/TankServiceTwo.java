package com.plumelog.demo.service;

import com.plumelog.trace.annotation.Trace;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * className：TankServiceTwo
 * description： TODO
 * time：2020-05-11.15:32
 *
 * @author Tank
 * @version 1.0.0
 */
@Service
public class TankServiceTwo {
    private static org.slf4j.Logger logger= LoggerFactory.getLogger(TankServiceTwo.class);
    @Trace
    public void tankServiceTwo(String data) {
        logger.info("tankServiceTwo==>>{}",data);
    }
}
