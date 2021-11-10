package com.plumelog.core;


import com.alibaba.ttl.TransmittableThreadLocal;
import com.plumelog.core.util.IdWorker;

import java.util.UUID;

/**
 * className：TraceId
 * description：TraceId 用来存储traceID相关信息
 * @author Frank.chen
 * @version 1.0.0
 */
public class TraceId {
    public static TransmittableThreadLocal<String> logTraceID = new TransmittableThreadLocal<String>();
    public static IdWorker worker = new IdWorker(1, 1, 1);

    public static void set() {
        logTraceID.set(String.valueOf(worker.nextId()));
    }

    public static void setSofa() {
        String traceId = TraceIdGenerator.generate();
        logTraceID.set(traceId);
    }

    public static String getTraceId(String traceId) {
        if (traceId == null || traceId.equals("")) {
            traceId = String.valueOf(worker.nextId());
            TraceId.logTraceID.set(traceId);
        }
        return traceId;
    }
}
