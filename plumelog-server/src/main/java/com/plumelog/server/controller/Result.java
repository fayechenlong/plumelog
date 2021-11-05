package com.plumelog.server.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * className：Result
 * description：
 * time：2020/6/10  19:20
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class Result {
    public static Result UN_LOGIN = new Result(401);
    public static Result INVALID_LOGIN = new Result(402);
    private Integer code;
    private String message;
    private List<String> logs = new ArrayList<>();
    private Object data;

    public Result() {
    }

    public Result(Integer code) {
        this.code = code;
    }

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
