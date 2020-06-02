package com.plumelog.core;


import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * @ClassName LogMessageThreadLocal
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/9 14:09
 * @Version 1.0
 **/
public class LogMessageThreadLocal {
    public static TransmittableThreadLocal<TraceMessage> logMessageThreadLocal = new TransmittableThreadLocal<>();
}
