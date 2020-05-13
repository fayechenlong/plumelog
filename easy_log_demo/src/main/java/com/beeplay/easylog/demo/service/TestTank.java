package com.beeplay.easylog.demo.service;

import com.beeplay.easylog.trace.annotation.Trace;
import org.springframework.stereotype.Service;

/**
 * className：TestTank
 * description： TODO
 * time：2020-05-12.19:05
 *
 * @author Tank
 * @version 1.0.0
 */
@Service
public class TestTank {


    @Trace
    public void testTank(Integer kk){
        System.out.println("testTank========>" + kk);
    }
}
