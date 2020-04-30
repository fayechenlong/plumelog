package com.beeplay.easylog.log4j.util;

import com.beeplay.easylog.core.LogMessage;
import com.beeplay.easylog.core.TransId;
import com.beeplay.easylog.core.util.DateUtil;
import com.beeplay.easylog.core.util.IpGetter;
import org.apache.log4j.spi.LoggingEvent;
import java.sql.Timestamp;

/**
* @Author Frank.chen
* @Description //TODO
* @Date 15:56 2020/4/27
**/
public class LogMessageUtil {

    /**
    * @Author Frank.chen
    * @Description //TODO
    * @Date 15:56 2020/4/27
    * @Param [appName, loggingEvent]
    * @return com.beeplay.easylog.core.LogMessage
    **/
    public static LogMessage getLogMessage(String appName,LoggingEvent loggingEvent){
        LogMessage logMessage = new LogMessage();
        String ip = IpGetter.getIp();
        logMessage.setServerName(ip);
        logMessage.setAppName(appName);
        logMessage.setContent(loggingEvent.getRenderedMessage());
        logMessage.setTransId(TransId.logTranID.get());
        logMessage.setDateTime(DateUtil.parseTimestampToStr(new Timestamp(loggingEvent.getTimeStamp()),DateUtil.DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS));
        logMessage.setDtTime(loggingEvent.getTimeStamp());
        logMessage.setClassName(loggingEvent.getLoggerName());
        logMessage.setMethod(loggingEvent.getLocationInformation().getMethodName());
        logMessage.setLogLevel(loggingEvent.getLevel().toString());
        return logMessage;

    }
}
