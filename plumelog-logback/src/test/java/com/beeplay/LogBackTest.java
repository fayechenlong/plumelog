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
            logger.error("我是中文");
        System.out.println("执行成功！");
    }
}
