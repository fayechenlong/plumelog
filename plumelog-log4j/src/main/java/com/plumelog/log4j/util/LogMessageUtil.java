package com.plumelog.log4j.util;

import com.plumelog.core.LogMessageThreadLocal;
import com.plumelog.core.TraceMessage;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.*;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * className：LogMessageUtil
 * description：组装日志对象
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class LogMessageUtil {

    /**
     * 序列生成器：当日志在一毫秒内打印多次时，发送到服务端排序时无法按照正常顺序显示，因此加一个序列保证同一毫秒内的日志按顺序显示
     * 使用AtomicLong不要使用LongAdder，LongAdder在该场景高并发下无法严格保证顺序性，也不需要考虑Long是否够用，假设每秒打印10万日志，也需要两百多万年才能用的完
     */
    private static final AtomicLong SEQ_BUILDER = new AtomicLong();

    public static BaseLogMessage getLogMessage(String appName, String env, LoggingEvent loggingEvent) {
        TraceMessage traceMessage = LogMessageThreadLocal.logMessageThreadLocal.get();
        String formattedMessage = getMessage(loggingEvent);
        if (formattedMessage.startsWith(LogMessageConstant.TRACE_PRE)) {
            return TraceLogMessageFactory.getTraceLogMessage(
                    traceMessage, appName, env, loggingEvent.getTimeStamp());
        }
        RunLogMessage logMessage =
                TraceLogMessageFactory.getLogMessage(appName, env, formattedMessage, loggingEvent.getTimeStamp());
        logMessage.setClassName(loggingEvent.getLoggerName());
        logMessage.setThreadName(loggingEvent.getThreadName());
        logMessage.setSeq(SEQ_BUILDER.getAndIncrement());

        LocationInfo locationInfo = loggingEvent.getLocationInformation();
        String method = locationInfo.getMethodName();
        String line = locationInfo.getLineNumber();
        logMessage.setMethod(method + "(" + locationInfo.getFileName() + ":" + line + ")");
        // dateTime字段用来保存当前服务器的时间戳字符串
        logMessage.setDateTime(DateUtil.getDatetimeNormalStrWithMills(loggingEvent.getTimeStamp()));

        logMessage.setLogLevel(loggingEvent.getLevel().toString());
        return logMessage;
    }

    /**
     * 扩展字段
     *
     * @param baseLogMessage
     * @param logEvent
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String getLogMessage(BaseLogMessage baseLogMessage, final LoggingEvent logEvent) {
        Map<String, String> mdc = logEvent.getProperties();
        Map<String, Object> map = StringUtils.entityToMap(baseLogMessage);
        if (mdc != null) {
            map.putAll(mdc);
        }
        return GfJsonUtil.toJSONString(map);
    }
    private static String getMessage(LoggingEvent logEvent) {
        if (logEvent.getLevel().toInt() == Priority.ERROR_INT || logEvent.getLevel().toInt() == Priority.WARN_INT) {
            String msg = "";
            if (logEvent.getThrowableInformation() != null) {
                msg = LogExceptionStackTrace.errorStackTrace(
                        logEvent.getThrowableInformation().getThrowable()).toString();
            }
            if (logEvent.getRenderedMessage() != null && logEvent.getRenderedMessage()
                    .contains(LogMessageConstant.DELIM_STR)) {
                FormattingTuple format = MessageFormatter.format(logEvent.getRenderedMessage(), msg);
                return format.getMessage();
            }
            return logEvent.getRenderedMessage() + "\n" + msg;
        }
        return logEvent.getRenderedMessage();
    }
}
