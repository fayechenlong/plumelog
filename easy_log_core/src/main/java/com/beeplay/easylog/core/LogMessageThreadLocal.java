package com.beeplay.easylog.core;


/**
 * @ClassName LogMessageThreadLocal
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/9 14:09
 * @Version 1.0
 **/
public class LogMessageThreadLocal {
    public static ThreadLocal<TraceMessage> logMessageThreadLocal = new ThreadLocal<>();
}
