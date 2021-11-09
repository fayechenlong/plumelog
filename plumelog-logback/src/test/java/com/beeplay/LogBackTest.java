package com.beeplay;

import com.plumelog.core.LogMessage;
import com.plumelog.core.util.LogExceptionStackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogBackTest {
    private static final Logger logger = LoggerFactory.getLogger("com.aa");
    private static final Logger logger1 = LoggerFactory.getLogger("com.bb");
    /**
     * @param args
     */
    public static void main(String[] args) {

        for(int i=0;i<Integer.MAX_VALUE;i++) {
            logger.info("远程调用成功！"+String.valueOf(i));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }
}
