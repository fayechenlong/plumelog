package com.beeplay;

import com.beeplay.easylog.core.TraceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LogBackTest {
    private static final Logger logger = LoggerFactory.getLogger(LogBackTest.class);
    /**
     * @param args
     */
    public static void main(String[] args) {

        TraceId.logTraceID.set(UUID.randomUUID().toString());
        logger.info("{}","I am log name:"+UUID.randomUUID().toString());
        logger.info("{}","I am log name:"+UUID.randomUUID().toString());
    }
}
