package com.beeplay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogBackTest {
    private static final Logger logger = LoggerFactory.getLogger("com.aa");
    private static final Logger logger1 = LoggerFactory.getLogger("com.bb");
    /**
     * @param args
     */
    public static void main(String[] args) {

        for(int i=0;i<100;i++) {
            logger.info("info");
            logger.error("eee");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }
}
