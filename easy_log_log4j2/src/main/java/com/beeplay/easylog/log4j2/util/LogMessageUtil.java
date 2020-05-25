package com.beeplay.easylog.log4j2.util;

import com.beeplay.easylog.core.LogMessageThreadLocal;
import com.beeplay.easylog.core.TraceMessage;
import com.beeplay.easylog.core.constant.LogMessageConstant;
import com.beeplay.easylog.core.dto.BaseLogMessage;
import com.beeplay.easylog.core.dto.RunLogMessage;
import com.beeplay.easylog.core.util.LogExceptionStackTrace;
import com.beeplay.easylog.core.util.TraceLogMessageFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;

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
        logMessage.setMethod(logEvent.getThreadName());
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
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof Throwable) {
                            args[i] = LogExceptionStackTrace.erroStackTrace(args[i]);
                        }
                    }
                    return packageMessage(logEvent.getMessage().getFormat(), args);
                }
            }
        }
        return logEvent.getMessage().getFormattedMessage();
    }

    private static String packageMessage(String message, Object[] args) {
        if (message.indexOf(LogMessageConstant.DELIM_STR) > 0) {
            return INSTANCE.newMessage(message, args).getFormattedMessage();
        }
        return TraceLogMessageFactory.packageMessage(message, args);
    }
}
