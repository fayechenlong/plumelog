package com.plumelog.core;


import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.UUID;

/**
 * className：TraceId
 * description：TraceId 用来存储traceID相关信息
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class TraceId {
    public static TransmittableThreadLocal<String> logTraceID = new TransmittableThreadLocal<String>();

    public static void set() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String traceid = uuid.substring(uuid.length() - 7);
        logTraceID.set(traceid);
    }

    public static void setSofa() {
        String traceid = TraceIdGenerator.generate();
        logTraceID.set(traceid);
    }
    public static void reset() {
        logTraceID.set(null);
    }
}
