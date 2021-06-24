package com.plumelog.log4j.util;

import com.plumelog.core.LogMessageThreadLocal;
import com.plumelog.core.TraceMessage;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.DateUtil;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.LogExceptionStackTrace;
import com.plumelog.core.util.TraceLogMessageFactory;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Map;

/**
 * className：LogMessageUtil
 * description：组装日志对象
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class LogMessageUtil {
    
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
     * @param baseLogMessage
     * @param logEvent
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String getLogMessage(BaseLogMessage baseLogMessage, final LoggingEvent logEvent) {
        Map<String, String> mdc = logEvent.getProperties();
        Map<String, Object> map = GfJsonUtil.parseObject(GfJsonUtil.toJSONString(baseLogMessage), Map.class);
        if (mdc != null) {
            map.putAll(mdc);
        }
        return GfJsonUtil.toJSONString(map);
    }
    private static String getMessage(LoggingEvent logEvent) {
        if (logEvent.getLevel().toInt() == Priority.ERROR_INT) {
            String msg = "";
            if (logEvent.getThrowableInformation() != null) {
                msg = LogExceptionStackTrace.erroStackTrace(
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
