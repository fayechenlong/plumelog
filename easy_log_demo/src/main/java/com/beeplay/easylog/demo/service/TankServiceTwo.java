package com.beeplay.easylog.demo.service;

import com.beeplay.easylog.trace.annotation.Trace;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    TestTankTwo testTankTwo;



    @Trace
    public void tankServiceTwo() {
        System.out.println("tankServiceTwo========>");
        testTankTwo.testTankTwo(998);
    }
}
