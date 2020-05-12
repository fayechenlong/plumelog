package com.beeplay.easylog.demo.service;

import com.beeplay.easylog.trace.annotation.Trace;
import org.springframework.stereotype.Service;

/**
 * className：TankServiceThere
 * description： TODO
 * time：2020-05-11.15:34
 *
 * @author Tank
 * @version 1.0.0
 */
@Service
public class TankServiceThere {

    @Trace
    public void tankServiceThere() {
        System.out.println("tankServiceThere========>");
    }
}
