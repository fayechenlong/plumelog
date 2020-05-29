package com.beeplay.easylog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * className：RestApiService
 * description： TODO
 * time：2020-05-29.15:28
 *
 * @author Tank
 * @version 1.0.0
 */
@Service
public class RestApiService {
    @Autowired
    FeignClientTest feignClientTest;

    public String getIndex(){
       return  feignClientTest.testFeign(100);
    }
}
