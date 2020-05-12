package com.beeplay.easylog.demo.service;

import com.beeplay.easylog.trace.annotation.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * className：TankService
 * description：
 * time：2020-05-11.14:06
 *
 * @author Tank
 * @version 1.0.0
 */
@Service
public class TankService {

    @Autowired
    TankServiceTwo tankServiceTwo;

    @Autowired
    TankServiceThere tankServiceThere;

    @Trace
    public void tankSay(String kk) {
        System.out.println("tankSay========>" + kk);
        tankServiceTwo.tankServiceTwo();
        tankServiceThere.tankServiceThere();
    }
}
