package com.plumelog.lite.client;

import com.plumelog.core.client.AbstractServerClient;
import com.plumelog.core.constant.LogMessageConstant;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * className：AutoDeleteLogs
 * description：自动删除日志，凌晨0点执行
 * time：2020/6/11 11:39
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@Component
public class AutoDeleteLogs {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(AutoDeleteLogs.class);

    @Autowired
    private AbstractServerClient abstractServerClient;

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteLogs() {
        if (InitConfig.keepDays > 0) {
            try {
                logger.info("begin delete {} days ago run logs!", InitConfig.keepDays);
                String runLogIndex = IndexUtil.getRunLogIndex(
                        System.currentTimeMillis() - InitConfig.keepDays * InitConfig.MILLS_ONE_DAY);
                abstractServerClient.deleteIndex(runLogIndex);
                for (int a = 0; a < 24; a++) {
                    String hour = String.format("%02d", a);
                    abstractServerClient.deleteIndex(runLogIndex + hour);
                }
                logger.info("delete success! index:" + runLogIndex);
            } catch (Exception e) {
                logger.error("delete logs error!", e);
            }
        } else {
            logger.info("unwanted delete logs");
        }
        if (InitConfig.traceKeepDays > 0) {
            try {
                logger.info("begin delete {} days ago trace logs!", InitConfig.traceKeepDays);
                String traceLogIndex = IndexUtil.getTraceLogIndex(
                        System.currentTimeMillis() - InitConfig.traceKeepDays * InitConfig.MILLS_ONE_DAY);
                abstractServerClient.deleteIndex(traceLogIndex);
                for (int a = 0; a < 24; a++) {
                    String hour = String.format("%02d", a);
                    abstractServerClient.deleteIndex(traceLogIndex + hour);
                }
                logger.info("delete success! index:" + traceLogIndex);
            } catch (Exception e) {
                logger.error("delete logs error!", e);
            }
        } else {
            logger.info("unwanted delete logs");
        }
    }
}
