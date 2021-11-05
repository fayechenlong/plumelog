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
            logger.warn("警告日志展示！");
            try {
                String a=null;
                a.equals("");
            }catch (Exception e){

                logger.error("错误:{}",LogExceptionStackTrace.erroStackTrace(e));
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }
}
