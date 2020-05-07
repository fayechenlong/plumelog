package com.beeplay.easylog.core;


public class TraceId {
    public static ThreadLocal<String> logTraceID = new ThreadLocal<String>();
}
