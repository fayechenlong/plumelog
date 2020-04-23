package com.beeplay.easylog.util;

import com.beeplay.easylog.LogMessage;
import org.apache.log4j.spi.LoggingEvent;
import java.sql.Timestamp;
import java.util.Map;


public class LogMessageUtil {

    public static LogMessage getLogMessage(final String appId,final LoggingEvent ev){
        final LogMessage lc = new LogMessage();
        String ip = IpGetter.getIp();
        lc.setServerName(ip);
        lc.setAppId(appId);
        Object message=ev.getMessage();
        if(message instanceof Map){
            StringBuffer sb = new StringBuffer();
            Map lo=(Map)ev.getMessage();
            sb.append(lo.get("content"));
            lc.setContent(sb.toString());
            lc.setTransId(lo.get("transId").toString());
        }else {
            lc.setContent(ev.getRenderedMessage());
        }
        Timestamp ts = new Timestamp(ev.getTimeStamp());
        lc.setDtTime(ts);
        int levelLog = ev.getLevel().toInt();
        lc.setClassName(ev.getLoggerName());
        lc.setMethod(ev.getThreadName());
        switch (levelLog) {
            case 10000:
                lc.setLogLevel("DBUG");
                break;
            case 20000:
                lc.setLogLevel("INFO");
                break;
            case 30000:
                lc.setLogLevel("WARN");
                break;
            case 40000:
                lc.setLogLevel("ERROR");
                break;
            case 50000:
                lc.setLogLevel("FATAL");
                break;
            default:
                break;
        }
        return lc;
    }
}
