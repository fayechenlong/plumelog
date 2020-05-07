package com.beeplay.easylog.logback.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.beeplay.easylog.core.LogMessage;
import com.beeplay.easylog.core.TraceId;
import com.beeplay.easylog.core.util.DateUtil;
import com.beeplay.easylog.core.util.IpGetter;
import java.sql.Timestamp;

public class LogMessageUtil {

    public static LogMessage getLogMessage(final String appName,final ILoggingEvent iLoggingEvent){
        LogMessage logMessage = new LogMessage();
        String ip = IpGetter.getIp();
        logMessage.setServerName(ip);
        logMessage.setAppName(appName);
        logMessage.setContent(iLoggingEvent.getFormattedMessage());
        logMessage.setTraceId(TraceId.logTraceID.get());
        logMessage.setDateTime(DateUtil.parseTimestampToStr(new Timestamp(iLoggingEvent.getTimeStamp()),DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS));
        logMessage.setDtTime(iLoggingEvent.getTimeStamp());
        logMessage.setClassName(iLoggingEvent.getLoggerName());
        logMessage.setMethod(iLoggingEvent.getThreadName());
        logMessage.setLogLevel(iLoggingEvent.getLevel().toString());
        return logMessage;
    }
}
