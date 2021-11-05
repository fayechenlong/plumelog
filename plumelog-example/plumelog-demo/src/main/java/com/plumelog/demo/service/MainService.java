package com.plumelog.demo.service;


import com.alibaba.ttl.threadpool.TtlExecutors;
import com.plumelog.core.LogMessage;
import com.plumelog.core.util.LogExceptionStackTrace;
import com.plumelog.trace.annotation.Trace;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class MainService {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(MainService.class);
    private static ExecutorService executorService = TtlExecutors.getTtlExecutorService(
            new ThreadPoolExecutor(8, 8,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>()));
    @Autowired
    TankService tankService;

   //@Reference
   //EasyLogDubboService easyLogDubboService;

    @Trace
    public void testLog(String data) {
//        logger.error("I am service! 下面调用EasyLogDubboService远程服务！");
        //easyLogDubboService.testLogDubbo();
        logger.info("远程调用成功！");

        logger.info("[CDATA[SUCCESS]]></result_code>\n" +
                "<return_code><![CDATA[SUCCESS]]></return_code>\n" +
                "<sign><![CDATA[D7010CE255B315D251751CD7ACC6FF5B]]></sign>\n" +
                "<time_end><![CDATA[20200909111054]]></time_end>\n" +
                "<total_fee>880</total_fee>\n" +
                "<trade_type><![CDATA[JSAPI]]></trade_type>\n" +
                "<transaction_id><![CDATA[4200000699202009099567739204]]></transaction_id>\n" +
                "</xml>");

        logger.info("<div class=\"pnl_login\">\n" +
                "                <div class=\"title\">登录</div>\n" +
                "                <div class=\"pnl_input\">\n" +
                "                    <input v-model=\"formData.username\" type=\"text\" placeholder=\"输入用户名\" id=\"txtUserName\" /><br/>\n" +
                "                    <input v-model=\"formData.password\" type=\"password\" placeholder=\"输入密码\" @keyup.enter=\"submit\" id=\"txtPassWord\" /><br/>\n" +
                "                    <input @click=\"submit\" type=\"button\" value=\"提交\" id=\"btn_login\"/>\n" +
                "                </div>\n" +
                "            </div>");

//        executorService.execute(() -> {
//            logger.info("子线程日志展示");
//        });

        MDC.put("orderNum","12345678");
        logger.info("订单编号：{}","12345678");

        try {
            LogMessage lo=null;
            lo.setMethod("");
            tankService.tankSay(data);

        }catch (Exception e){
            logger.error("异常日志展示", LogExceptionStackTrace.erroStackTrace(e));
        }
        logger.warn("警告日志展示！");
    }
//    @Scheduled(cron = "0/1 * * * * ?")
//    public void test(){
//
//        logger.info("正常");
//        logger.warn("远程调用成功");
//        logger.error("11");
//    }
}
