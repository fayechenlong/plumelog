package com.beeplay.easylog.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class MainController {

    @RequestMapping("/index")
    private String index(){
        return "index";
    }
    @RequestMapping("/index1")
    private String index1(){
        return "index1";
    }
    @RequestMapping("/index2")
    private String index2(){
        return "index2";
    }

}
