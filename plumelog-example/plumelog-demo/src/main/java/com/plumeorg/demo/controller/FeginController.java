package com.plumeorg.demo.controller;

import com.plumeorg.demo.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * className：FeginController
 * description： TODO
 * time：2020-05-29.15:36
 *
 * @author Tank
 * @version 1.0.0
 */
@RestController
public class FeginController {

    @Autowired
    private MainService mainService;

    @GetMapping("/test/feign/{id}")
    public String testFeign(@RequestParam("id") Integer id) {
        mainService.testLog("testFeign" + id);
        return "testFeign" + id;
    }
}
