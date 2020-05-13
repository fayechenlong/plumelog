package com.beeplay.easylog.log4j2.util;

import com.beeplay.easylog.core.LogMessage;
import com.beeplay.easylog.core.LogMessageThreadLocal;
import com.beeplay.easylog.core.TraceId;
import com.beeplay.easylog.core.TraceMessage;
import com.beeplay.easylog.core.constant.LogMessageConstant;
import com.beeplay.easylog.core.dto.BaseLogMessage;
import com.beeplay.easylog.core.dto.RunLogMessage;
import com.beeplay.easylog.core.util.DateUtil;
import com.beeplay.easylog.core.util.IpGetter;
import com.beeplay.easylog.core.util.TraceLogMessageFactory;
import org.apache.logging.log4j.core.LogEvent;

import java.sql.Timestamp;

/**
 * 组装日志数据
 */
public class LogMessageUtil {

    public static BaseLogMessage getLogMessage(String appName, LogEvent logEvent){
        TraceMessage traceMessage = LogMessageThreadLocal.logMessageThreadLocal.get();
        String formattedMessage = logEvent.getMessage().getFormattedMessage();
        if (formattedMessage.startsWith(LogMessageConstant.TRACE_PRE)) {
            return TraceLogMessageFactory.getTraceLogMessage(
                    traceMessage,appName,logEvent.getTimeMillis());
        }
        RunLogMessage logMessage = new RunLogMessage();
        String ip = IpGetter.getIp();
        logMessage.setServerName(ip);
        logMessage.setAppName(appName);
        logMessage.setContent(logEvent.getMessage().getFormattedMessage());
        logMessage.setTraceId(TraceId.logTraceID.get());
        logMessage.setDateTime(DateUtil.parseTimestampToStr(new Timestamp(logEvent.getTimeMillis()),DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS));
        logMessage.setDtTime(logEvent.getTimeMillis());
        logMessage.setClassName(logEvent.getSource().getClassName());
        logMessage.setMethod(logEvent.getSource().getMethodName());
        logMessage.setLogLevel(logEvent.getLevel().toString());
        return logMessage;
    }
}
