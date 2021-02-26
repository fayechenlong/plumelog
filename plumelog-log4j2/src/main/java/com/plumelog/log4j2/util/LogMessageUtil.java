package com.plumelog.log4j2.util;

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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;

import java.util.Date;
import java.util.Map;

import static org.apache.logging.log4j.message.ParameterizedMessageFactory.INSTANCE;


/**
 * className：TraceAspect
 * description：
 * time：2020-05-19.14:34
 *
 * @author Tank
 * @version 1.0.0
 */
public class LogMessageUtil {

    private static String isExpandRunLog(LogEvent logEvent) {
        String traceId = null;
        if (LogMessageConstant.EXPAND.equals(LogMessageConstant.SLEUTH_EXPAND)) {
            if (!logEvent.getContextData().isEmpty()) {
                traceId = logEvent.getContextData().toMap().get(LogMessageConstant.TRACE_ID);
                TraceId.logTraceID.set(traceId);
            }
        }
        return traceId;
    }

    /**
     * 扩展字段
     *
     * @param baseLogMessage
     * @param logEvent
     * @return
     */
    public static String getLogMessage(BaseLogMessage baseLogMessage, final LogEvent logEvent) {
        Map<String, String> mdc = logEvent.getContextData().toMap();
        Map<String, Object> map = GfJsonUtil.parseObject(GfJsonUtil.toJSONString(baseLogMessage), Map.class);
        if (mdc != null) {
            map.putAll(mdc);
        }
        ;
        return GfJsonUtil.toJSONString(map);
    }

    public static BaseLogMessage getLogMessage(String appName, LogEvent logEvent) {
        TraceMessage traceMessage = LogMessageThreadLocal.logMessageThreadLocal.get();
        String formattedMessage = getMessage(logEvent);
        if (formattedMessage.startsWith(LogMessageConstant.TRACE_PRE)) {
            return TraceLogMessageFactory.getTraceLogMessage(
                    traceMessage, appName, logEvent.getTimeMillis());
        }
        RunLogMessage logMessage =
                TraceLogMessageFactory.getLogMessage(appName, formattedMessage, logEvent.getTimeMillis());
        logMessage.setClassName(logEvent.getLoggerName());

        StackTraceElement stackTraceElement = logEvent.getSource();
        String method = stackTraceElement.getMethodName();
        String line = String.valueOf(stackTraceElement.getLineNumber());
        logMessage.setMethod(method + "(" + stackTraceElement.getFileName() + ":" + line + ")");
        logMessage.setDateTime(DateUtil.parseDateToStr(new Date(logEvent.getTimeMillis()), DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI));

        logMessage.setLogLevel(logEvent.getLevel().toString());
        return logMessage;
    }

    private static String getMessage(LogEvent logEvent) {
        if (logEvent.getLevel().equals(Level.ERROR)) {
            Object[] msg = new String[1];
            if (logEvent.getThrown() != null) {
                msg[0] = LogExceptionStackTrace.erroStackTrace(logEvent.getThrown()).toString();
                return packageMessage(logEvent.getMessage().getFormat(), msg);
            } else {
                if (logEvent.getMessage().getParameters() != null) {
                    Object[] args = logEvent.getMessage().getParameters();
                    if (args != null) {
                        for (int i = 0; i < args.length; i++) {
                            if (args[i] instanceof Throwable) {
                                args[i] = LogExceptionStackTrace.erroStackTrace(args[i]);
                            }
                        }
                        return packageMessage(logEvent.getMessage().getFormat(), args);
                    }
                }
            }
        }
        return logEvent.getMessage().getFormattedMessage();
    }

    private static String packageMessage(String message, Object[] args) {
        if (message != null && message.indexOf(LogMessageConstant.DELIM_STR) > -1) {
            return INSTANCE.newMessage(message, args).getFormattedMessage();
        }
        return TraceLogMessageFactory.packageMessage(message, args);
    }
}
