package com.beeplay.easylog.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * className：FeignClientTest
 * description： TODO
 * time：2020-05-29.15:29
 *
 * @author Tank
 * @version 1.0.0
 */
@FeignClient(name = "feignClient",url = "http://localhost:8080")
public interface FeignClientTest {


    @GetMapping("/test/feign/{id}")
    String testFeign(@RequestParam("id") Integer id);
}
