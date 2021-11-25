package com.beeplay;

import com.plumelog.core.TraceId;
import com.plumelog.core.util.LogExceptionStackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogBackTest {
    private static final Logger logger = LoggerFactory.getLogger("com.jf");
    private static final Logger logger1 = LoggerFactory.getLogger("com.jf");
    /**
     * @param args
     */
    public static void main(String[] args) {

        for(int i=0;i<Integer.MAX_VALUE;i++) {
            TraceId.set();
            logger.info("用户开始浏览商品！商品名称：「iphone13 pro max」");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            logger.debug("用户开始下单！");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            logger.warn("开始支付！");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            logger.info("支付成功！");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            MDC.put("orderId","DA112121212"+String.valueOf(i));
            logger.warn("回掉失败，扣款可能未成功");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
            logger.error("出现未知异常，扣款失败！");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }
}
