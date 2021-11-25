package com.plumeorg.demo.service;


/**
 * className：GolExection
 * description： TODO
 * time：2020-05-27.15:46
 *
 * @author Tank
 * @version 1.0.0
 */
public class GolExection extends Exception {

    private static final long serialVersionUID = 1L;

    private String message;

    private int code = 100;

    public GolExection(){
        super();
    }

    public GolExection(String message,  int code) {
        this.message = message;
        this.code = code;
    }

    public GolExection(String message){
        super(message);
    }

    public GolExection(Throwable e){
        super(e);
    }

    public GolExection(String message, Throwable e){
        super(message, e);
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
