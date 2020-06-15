package com.plumelog.server.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * className：Result
 * description：
 * time：2020/6/10  19:20
 *
 * @author jdd
 * @version 1.0.0
 */
public class Result {
    private Integer code;
    private String message;
    private List<String> logs = new ArrayList<>();

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }
}
