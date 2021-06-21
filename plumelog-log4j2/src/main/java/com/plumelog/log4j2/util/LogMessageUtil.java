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
        if (!logEvent.getContextData().isEmpty()) {
            traceId = logEvent.getContextData().toMap().get(LogMessageConstant.TRACE_ID);
            TraceId.logTraceID.set(traceId);
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
    @SuppressWarnings("unchecked")
    public static String getLogMessage(BaseLogMessage baseLogMessage, final LogEvent logEvent) {
        Map<String, String> mdc = logEvent.getContextData().toMap();
        Map<String, Object> map = GfJsonUtil.parseObject(GfJsonUtil.toJSONString(baseLogMessage), Map.class);
        if (mdc != null) {
            map.putAll(mdc);
        }
        return GfJsonUtil.toJSONString(map);
    }

    public static BaseLogMessage getLogMessage(String appName, String env, LogEvent logEvent) {
        isExpandRunLog(logEvent);
        TraceMessage traceMessage = LogMessageThreadLocal.logMessageThreadLocal.get();
        String formattedMessage = getMessage(logEvent);
        if (formattedMessage.startsWith(LogMessageConstant.TRACE_PRE)) {
            return TraceLogMessageFactory.getTraceLogMessage(
                    traceMessage, appName, env, logEvent.getTimeMillis());
        }
        RunLogMessage logMessage =
                TraceLogMessageFactory.getLogMessage(appName, env, formattedMessage, logEvent.getTimeMillis());
        logMessage.setClassName(logEvent.getLoggerName());

        StackTraceElement stackTraceElement = logEvent.getSource();
        String method = stackTraceElement.getMethodName();
        String line = String.valueOf(stackTraceElement.getLineNumber());
        logMessage.setMethod(method + "(" + stackTraceElement.getFileName() + ":" + line + ")");
        // dateTime字段用来保存当前服务器的时间戳字符串
        logMessage.setDateTime(DateUtil.getDatetimeNormalStrWithMills(logEvent.getTimeMillis()));

        logMessage.setLogLevel(logEvent.getLevel().toString());
        return logMessage;
    }

    private static String getMessage(LogEvent logEvent) {
        if (logEvent.getLevel().equals(Level.ERROR)) {
            // 如果占位符个数与参数个数相同,即使最后一个参数为Throwable类型,logEvent.getThrown()也会为null
            Throwable thrown = logEvent.getThrown();
            String formatMessage = logEvent.getMessage().getFormat();
            Object[] args = logEvent.getMessage().getParameters();
            if (args != null) {
                for (int i = 0, l = args.length; i < l; i++) {
                    // 当最后一个参数与thrown是同一个对象时,表示logEvent.getThrown()不为null,并且占位符个数与参数个数不相同,则将该thrown留后处理
                    if ((i != l - 1 || args[i] != thrown) && args[i] instanceof Throwable) {
                        args[i] = LogExceptionStackTrace.erroStackTrace(args[i]);
                    }
                }
                formatMessage = packageMessage(formatMessage, args);
            }
            if (thrown != null) {
                return packageMessage(formatMessage,
                        new String[]{LogExceptionStackTrace.erroStackTrace(thrown).toString()});
            }
            return formatMessage;
        }
        return logEvent.getMessage().getFormattedMessage();
    }

    private static String packageMessage(String message, Object[] args) {
        if (message != null && message.contains(LogMessageConstant.DELIM_STR)) {
            return INSTANCE.newMessage(message, args).getFormattedMessage();
        }
        return TraceLogMessageFactory.packageMessage(message, args);
    }
}
