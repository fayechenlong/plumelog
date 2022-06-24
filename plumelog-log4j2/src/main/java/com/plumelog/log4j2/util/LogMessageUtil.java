package com.plumelog.log4j2.util;

import com.plumelog.core.LogMessageThreadLocal;
import com.plumelog.core.TraceId;
import com.plumelog.core.TraceMessage;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.logging.log4j.message.ParameterizedMessageFactory.INSTANCE;

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

    private static String isExpandRunLog(LogEvent logEvent) {
        String traceId = null;
        if (!logEvent.getContextData().isEmpty()) {
            traceId = logEvent.getContextData().toMap().get(LogMessageConstant.TRACE_ID);
            if (traceId != null) {
                TraceId.logTraceID.set(traceId);
            }
        }
        return traceId;
    }

    public static String getLogMessage(RunLogMessage runLogMessage, final LogEvent logEvent) {
        Map<String, String> mdc = logEvent.getContextData().toMap();
        Map<String, Object> map = StringUtils.entityToMap(runLogMessage);
        if (mdc != null) {
            map.putAll(mdc);
        }
        return GfJsonUtil.toJSONString(map);
    }

    public static BaseLogMessage getLogMessage(String appName, String env, LogEvent logEvent,String runModel) {
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
        logMessage.setThreadName(logEvent.getThreadName());
        logMessage.setSeq(SEQ_BUILDER.getAndIncrement());
        StackTraceElement stackTraceElement=null;
        if("2".equals(runModel)) {
            stackTraceElement = logEvent.getSource();
        }
        if (stackTraceElement != null) {
            String method = stackTraceElement.getMethodName();
            String line = String.valueOf(stackTraceElement.getLineNumber());
            logMessage.setMethod(method + "(" + stackTraceElement.getFileName() + ":" + line + ")");
        } else {
            logMessage.setMethod(logEvent.getThreadName());
        }
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
                        args[i] = LogExceptionStackTrace.errorStackTrace(args[i]);
                    }
                }
                formatMessage = packageMessage(formatMessage, args);
            }
            if (thrown != null) {
                return packageMessage(formatMessage,
                        new String[]{LogExceptionStackTrace.errorStackTrace(thrown).toString()});
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
