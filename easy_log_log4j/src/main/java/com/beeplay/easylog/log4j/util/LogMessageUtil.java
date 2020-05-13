package com.beeplay.easylog.log4j.util;

import com.beeplay.easylog.core.LogMessage;
import com.beeplay.easylog.core.LogMessageThreadLocal;
import com.beeplay.easylog.core.TraceId;
import com.beeplay.easylog.core.TraceMessage;
import com.beeplay.easylog.core.constant.LogMessageConstant;
import com.beeplay.easylog.core.dto.BaseLogMessage;
import com.beeplay.easylog.core.dto.RunLogMessage;
import com.beeplay.easylog.core.dto.TraceLogMessage;
import com.beeplay.easylog.core.util.DateUtil;
import com.beeplay.easylog.core.util.IpGetter;
import com.beeplay.easylog.core.util.TraceLogMessageFactory;
import org.apache.log4j.spi.LoggingEvent;

import java.sql.Timestamp;

/**
 * @Author Frank.chen
 * @Description //TODO
 * @Date 15:56 2020/4/27
 **/
public class LogMessageUtil {

    /**
     * @return com.beeplay.easylog.core.LogMessage
     * @Author Frank.chen
     * @Description //TODO
     * @Date 15:56 2020/4/27
     * @Param [appName, loggingEvent]
     **/
    public static BaseLogMessage getLogMessage(String appName, LoggingEvent loggingEvent) {
        TraceMessage traceMessage = LogMessageThreadLocal.logMessageThreadLocal.get();
        String formattedMessage = loggingEvent.getRenderedMessage();
        if (formattedMessage.startsWith(LogMessageConstant.TRACE_PRE)) {
            return TraceLogMessageFactory.getTraceLogMessage(
                    traceMessage,appName,loggingEvent.getTimeStamp());
        }
        RunLogMessage logMessage = new RunLogMessage();
        String ip = IpGetter.getIp();
        logMessage.setServerName(ip);
        logMessage.setAppName(appName);
        logMessage.setContent(loggingEvent.getRenderedMessage());
        logMessage.setTraceId(TraceId.logTraceID.get());
        logMessage.setDateTime(DateUtil.parseTimestampToStr(new Timestamp(loggingEvent.getTimeStamp()), DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS));
        logMessage.setDtTime(loggingEvent.getTimeStamp());
        logMessage.setClassName(loggingEvent.getLoggerName());
        logMessage.setMethod(loggingEvent.getLocationInformation().getMethodName());
        logMessage.setLogLevel(loggingEvent.getLevel().toString());
        return logMessage;
    }
}
