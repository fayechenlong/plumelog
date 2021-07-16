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

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * className：LogMessageUtil
 * description：
 * time：2020-05-19.14:34
 *
 * @author Tank
 * @version 1.0.0
 */
public class LogMessageUtil {

    /**
     * 序列生成器：当日志在一毫秒内打印多次时，发送到服务端排序时无法按照正常顺序显示，因此加一个序列保证同一毫秒内的日志按顺序显示
     * 使用AtomicLong不要使用LongAdder，LongAdder在该场景高并发下无法严格保证顺序性，也不需要考虑Long是否够用，假设每秒打印10万日志，也需要两百多万年才能用的完
     */
    private static final AtomicLong SEQ_BUILDER = new AtomicLong();

    private static String isExpandRunLog(ILoggingEvent logEvent) {
        String traceId = null;
        if (!logEvent.getMDCPropertyMap().isEmpty()) {
            traceId = logEvent.getMDCPropertyMap().get(LogMessageConstant.TRACE_ID);
            if(traceId!=null) {
                TraceId.logTraceID.set(traceId);
            }
        }
        return traceId;
    }

    /**
     * 扩展字段
     *
     * @param baseLogMessage
     * @param iLoggingEvent
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String getLogMessage(BaseLogMessage baseLogMessage, final ILoggingEvent iLoggingEvent) {
        Map<String, String> mdc = iLoggingEvent.getMDCPropertyMap();
        Map<String, Object> map = GfJsonUtil.parseObject(GfJsonUtil.toJSONString(baseLogMessage), Map.class);
        if (mdc != null) {
            map.putAll(mdc);
        }
        return GfJsonUtil.toJSONString(map);
    }

    public static BaseLogMessage getLogMessage(final String appName, final String env, final ILoggingEvent iLoggingEvent) {
        isExpandRunLog(iLoggingEvent);
        TraceMessage traceMessage = LogMessageThreadLocal.logMessageThreadLocal.get();
        String formattedMessage = getMessage(iLoggingEvent);
        if (formattedMessage.startsWith(LogMessageConstant.TRACE_PRE)) {
            return TraceLogMessageFactory.getTraceLogMessage(
                    traceMessage, appName, env, iLoggingEvent.getTimeStamp());
        }
        RunLogMessage logMessage =
                TraceLogMessageFactory.getLogMessage(appName, env, formattedMessage, iLoggingEvent.getTimeStamp());
        logMessage.setClassName(iLoggingEvent.getLoggerName());
        logMessage.setThreadName(iLoggingEvent.getThreadName());
        logMessage.setSeq(SEQ_BUILDER.getAndIncrement());

        StackTraceElement stackTraceElement = iLoggingEvent.getCallerData()[0];
        String method = stackTraceElement.getMethodName();
        String line = String.valueOf(stackTraceElement.getLineNumber());
        logMessage.setMethod(method + "(" + stackTraceElement.getFileName() + ":" + line + ")");
        // dateTime字段用来保存当前服务器的时间戳字符串
        logMessage.setDateTime(DateUtil.getDatetimeNormalStrWithMills(iLoggingEvent.getTimeStamp()));

        logMessage.setLogLevel(iLoggingEvent.getLevel().toString());
        return logMessage;
    }

    private static String getMessage(ILoggingEvent logEvent) {
        if (logEvent.getLevel().equals(Level.ERROR)) {
            if (logEvent.getThrowableProxy() != null) {
                ThrowableProxy throwableProxy = (ThrowableProxy) logEvent.getThrowableProxy();
                String[] args = new String[]{logEvent.getFormattedMessage() + "\n" + LogExceptionStackTrace.erroStackTrace(throwableProxy.getThrowable()).toString()};
                return packageMessage("{}", args);
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
        if (message != null && message.contains(LogMessageConstant.DELIM_STR)) {
            return MessageFormatter.arrayFormat(message, args).getMessage();
        }
        return TraceLogMessageFactory.packageMessage(message, args);
    }
}
