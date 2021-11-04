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
                throw new RuntimeException("");
            }catch (Exception e){
                logger.error("异常日志展示", LogExceptionStackTrace.erroStackTrace(e));
            }
            logger.warn("警告日志展示！");

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.error("", e);
            }
        }
    }
}
