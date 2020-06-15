package com.beeplay;


import com.plumelog.core.TraceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LogTest
{
    private static Logger logger=LoggerFactory.getLogger(LogTest.class);
    public static void main( String[] args )
    {
        TraceId.logTraceID.set(UUID.randomUUID().toString());
        logger.info("{}","I am log name:"+UUID.randomUUID().toString());
        logger.info("{}","I am log name:"+UUID.randomUUID().toString());
        //logger.error(null);
    }
}
