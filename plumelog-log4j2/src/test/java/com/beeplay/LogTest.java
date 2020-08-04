package com.beeplay;


import com.plumelog.core.TraceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

public class LogTest
{
    private static Logger logger=LoggerFactory.getLogger(LogTest.class);
    public static void main( String[] args )
    {
        for(int a=0;a<100000;a++) {
            TraceId.logTraceID.set(UUID.randomUUID().toString().replace("-", ""));
            MDC.put("orderid", "1");
            MDC.put("userid", "4");
            MDC.put("sheis", "3");
            logger.info("{}{}", "tongji",a);
        }
    }
}
