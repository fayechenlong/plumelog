package com.plumelog.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/plumelog")
    public String index1() {
        return "index";
    }
    @RequestMapping("/webConsole")
    public String webConsole() {
        return "webConsole";
    }
}
