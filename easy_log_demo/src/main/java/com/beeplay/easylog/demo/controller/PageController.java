package com.beeplay.easylog.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @ClassName PageController
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/4/28 13:41
 * @Version 1.0
 **/
@Controller
public class PageController {

    @RequestMapping("/")
    public String index(String data) {
        return "index";
    }
}
