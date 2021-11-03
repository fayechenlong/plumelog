package com.plumelog.lite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PlumeLogPageController {
    @RequestMapping("/plumelog")
    public String index() {
        return "plumelog";
    }
    @RequestMapping("/webConsole")
    public String webConsole() {
        return "webConsole";
    }
}
