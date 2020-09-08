package com.beeplay;

import com.plumelog.core.TraceId;
import com.plumelog.core.util.DateUtil;
import com.plumelog.core.util.IdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Date;
import java.util.UUID;

public class LogBackTest {
    private static final Logger logger = LoggerFactory.getLogger(LogBackTest.class);
    /**
     * @param args
     */
    public static void main(String[] args) {

        for(int a=0;a<100;a++) {
            try {
                Thread.sleep(1000);
            }catch (Exception e){}
            logger.error("1111");
        }
    }
}
