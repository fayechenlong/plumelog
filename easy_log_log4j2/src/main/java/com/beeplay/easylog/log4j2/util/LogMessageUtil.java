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
import org.apache.logging.log4j.message.Message;

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
        logMessage.setClassName(logEvent.getSource().getClassName());
        logMessage.setMethod(logEvent.getSource().getMethodName());
        logMessage.setLogLevel(logEvent.getLevel().toString());
        return logMessage;
    }

    public static String getMessage(LogEvent logEvent) {
        if (logEvent.getLevel().equals(Level.ERROR)) {
            String msg = null;
            if (logEvent.getThrown() != null) {
                msg = LogExceptionStackTrace.erroStackTrace(logEvent.getThrown()).toString();
                Message message = INSTANCE.newMessage(logEvent.getMessage().getFormat(), msg);
                return message.getFormattedMessage();

            }else {
                if (logEvent.getMessage().getParameters() != null) {
                    Object[] args = logEvent.getMessage().getParameters();
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof Throwable) {
                            args[i] = LogExceptionStackTrace.erroStackTrace(args[i]);
                        }
                    }
                    Message message = INSTANCE.newMessage(logEvent.getMessage().getFormat(), args);
                    return message.getFormattedMessage();
                }
            }
        }
        return logEvent.getMessage().getFormattedMessage();
    }
}
