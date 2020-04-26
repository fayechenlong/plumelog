package com.beeplay;


import com.beeplay.easylog.core.TransId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LogTest
{
    private static Logger logger=LoggerFactory.getLogger(LogTest.class);
    public static void main( String[] args )
    {
        TransId.logTranID.set(UUID.randomUUID().toString());
        logger.info("{}","I am log name:"+UUID.randomUUID().toString());
        logger.info("{}","I am log name:"+UUID.randomUUID().toString());
    }
}
