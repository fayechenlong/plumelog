package com.beeplay.easylog.demo.service;

import com.beeplay.easylog.trace.annotation.Trace;
import org.springframework.stereotype.Service;

/**
 * className：TestTankTwo
 * description： TODO
 * time：2020-05-12.19:10
 *
 * @author Tank
 * @version 1.0.0
 */
@Service
public class TestTankTwo {


    @Trace
    public void testTankTwo(Integer kk){
        System.out.println("testTankTwo========>" + kk);
    }
}
