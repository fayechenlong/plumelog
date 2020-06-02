package com.plumelog.core;


import com.alibaba.ttl.TransmittableThreadLocal;

public class TraceId {
    public static TransmittableThreadLocal<String> logTraceID = new TransmittableThreadLocal<String>();
}
