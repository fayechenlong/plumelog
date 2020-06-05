package com.plumelog.core;


import com.alibaba.ttl.TransmittableThreadLocal;
/**
 * className：TraceId
 * description：TraceId 用来存储traceID相关信息
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class TraceId {
    public static TransmittableThreadLocal<String> logTraceID = new TransmittableThreadLocal<String>();
}
