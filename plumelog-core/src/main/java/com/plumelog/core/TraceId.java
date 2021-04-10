package com.plumelog.core;


import com.alibaba.ttl.TransmittableThreadLocal;
import com.plumelog.core.util.IdWorker;

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
        String traceid= uuid.substring(uuid.length() - 14);
        logTraceID.set(traceid);
    }
    public static void setSofa() {
        String traceid= TraceIdGenerator.generate();
        logTraceID.set(traceid);
    }

    /**
     * if traceId is empty to reset
     * @param traceId the trace
     * @return new or old traceId
     */
    public static String getTraceId(String traceId) {
        if (traceId == null || traceId.equals("")) {
            IdWorker worker = new IdWorker(1,1,1);
            traceId = String.valueOf(worker.nextId());
            TraceId.logTraceID.set(traceId);
        }
        return traceId;
    }
}
