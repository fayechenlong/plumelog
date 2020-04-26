package com.beeplay.easylog.core;


public class TransId {
    public static ThreadLocal<String> logTranID = new ThreadLocal<String>();
}
