package com.plumelog.logback.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.plumelog.core.LogMessageThreadLocal;
import com.plumelog.core.TraceId;
import com.plumelog.core.TraceMessage;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.DateUtil;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.LogExceptionStackTrace;
import com.plumelog.core.util.TraceLogMessageFactory;
import org.slf4j.helpers.MessageFormatter;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * className：TraceAspect
 * description：
 * time：2020-05-19.14:34
 *
 * @author Tank
 * @version 1.0.0
 */
public class LogMessageUtil {

    private static String isExpandRunLog(ILoggingEvent logEvent) {
        String traceId = null;
        if (LogMessageConstant.EXPAND.equals(LogMessageConstant.SLEUTH_EXPAND)) {
            if (!logEvent.getMDCPropertyMap().isEmpty()) {
                traceId = logEvent.getMDCPropertyMap().get(LogMessageConstant.TRACE_ID);
                TraceId.logTraceID.set(traceId);
            }
        }
        return traceId;
    }

    /**
     * 扩展字段
     * @param baseLogMessage
     * @param iLoggingEvent
     * @return
     */
    public static String getLogMessage(BaseLogMessage baseLogMessage,final ILoggingEvent iLoggingEvent){
        Map<String, String> mdc= iLoggingEvent.getMDCPropertyMap();
        Map<String, Object> map=GfJsonUtil.parseObject(baseLogMessage.toString(),Map.class);
        if(mdc!=null) {
            map.putAll(mdc);
        }
        return GfJsonUtil.toJSONString(map);
    }

    public static BaseLogMessage getLogMessage(final String appName, final ILoggingEvent iLoggingEvent) {
        String traceId = isExpandRunLog(iLoggingEvent);
        TraceMessage traceMessage = LogMessageThreadLocal.logMessageThreadLocal.get();
        String formattedMessage = getMessage(iLoggingEvent);
        if (formattedMessage.startsWith(LogMessageConstant.TRACE_PRE)) {
            if (!LogMessageConstant.EXPAND.equals(LogMessageConstant.DEFAULT_EXPAND) && traceId != null) {
                traceMessage.setTraceId(traceId);
            }
            return TraceLogMessageFactory.getTraceLogMessage(
                    traceMessage, appName, iLoggingEvent.getTimeStamp());
        }
        RunLogMessage logMessage =
                TraceLogMessageFactory.getLogMessage(appName, formattedMessage, iLoggingEvent.getTimeStamp());
        logMessage.setClassName(iLoggingEvent.getLoggerName());
        if (LogMessageConstant.RUN_MODEL == 1) {
            logMessage.setMethod(iLoggingEvent.getThreadName());
        } else {
            StackTraceElement atackTraceElement = iLoggingEvent.getCallerData()[0];
            String method = atackTraceElement.getMethodName();
            String line = String.valueOf(atackTraceElement.getLineNumber());
            logMessage.setMethod(method + "(" +atackTraceElement.getFileName()+":"+ line + ")");
            logMessage.setDateTime(DateUtil.parseDateToStr(new Date(iLoggingEvent.getTimeStamp()), DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI));
        }
        logMessage.setLogLevel(iLoggingEvent.getLevel().toString());
        return logMessage;
    }

    private static String getMessage(ILoggingEvent logEvent) {
        if (logEvent.getLevel().equals(Level.ERROR)) {
            if (logEvent.getThrowableProxy() != null) {
                ThrowableProxy throwableProxy = (ThrowableProxy) logEvent.getThrowableProxy();
                String[] args = new String[]{LogExceptionStackTrace.erroStackTrace(throwableProxy.getThrowable()).toString()};
                return packageMessage(logEvent.getMessage(), args);
            } else {
                Object[] args = logEvent.getArgumentArray();
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof Throwable) {
                            args[i] = LogExceptionStackTrace.erroStackTrace(args[i]);
                        }
                    }
                    return packageMessage(logEvent.getMessage(), args);
                }
            }
        }
        return logEvent.getFormattedMessage();
    }

    private static String packageMessage(String message, Object[] args) {
        if (message!=null&&message.indexOf(LogMessageConstant.DELIM_STR) > -1) {
            return MessageFormatter.arrayFormat(message, args).getMessage();
        }
        return TraceLogMessageFactory.packageMessage(message, args);
    }
}
