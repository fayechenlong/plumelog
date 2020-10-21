package com.plumelog.core.util;

import com.plumelog.core.TraceId;
import com.plumelog.core.TraceMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.dto.TraceLogMessage;

/**
 * className：TraceLogMessageFactory
 * description： TODO
 * time：2020-05-13.14:04
 *
 * @author Tank
 * @version 1.0.0
 */
public class TraceLogMessageFactory<T> {

    public static TraceLogMessage getTraceLogMessage(TraceMessage traceMessage, String appName, String srvName, long time) {
        TraceLogMessage traceLogMessage = new TraceLogMessage();
        traceLogMessage.setAppName(appName);
        traceLogMessage.setTraceId(traceMessage.getTraceId());
        traceLogMessage.setMethod(traceMessage.getMessageType());
        traceLogMessage.setTime(time);
        traceLogMessage.setPosition(traceMessage.getPosition());
        traceLogMessage.setPositionNum(traceMessage.getPositionNum().get());
        traceLogMessage.setServerName(srvName);
        return traceLogMessage;
    }

    public static RunLogMessage getLogMessage(String appName, String srvName, String message, long time) {
        RunLogMessage logMessage = new RunLogMessage();
        logMessage.setServerName(srvName);
        logMessage.setAppName(appName);
        logMessage.setContent(message);
        logMessage.setDtTime(time);
        logMessage.setTraceId(TraceId.logTraceID.get());
        return logMessage;
    }

    public static String packageMessage(String message, Object[] args) {
        StringBuilder builder = new StringBuilder(128);
        builder.append(message);
        for (int i = 0; i < args.length; i++) {
            builder.append("\n").append(args[i]);
        }
        return builder.toString();
    }

}
