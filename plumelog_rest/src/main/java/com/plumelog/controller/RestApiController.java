package com.plumelog.controller;


import com.plumelog.service.RestApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * className：RestApiController
 * description： TODO
 * time：2020-05-29.15:14
 *
 * @author Tank
 * @version 1.0.0
 */
@RestController
public class RestApiController {

    @Autowired
    RestApiService restApiService;


    @GetMapping
    public String sayHello(){
        return restApiService.getIndex();
    }
}
