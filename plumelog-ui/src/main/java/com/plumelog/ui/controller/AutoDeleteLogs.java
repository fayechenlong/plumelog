package com.plumelog.ui.controller;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.util.DateUtil;
import com.plumelog.ui.es.ElasticLowerClient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * className：AutoDeleteLogs
 * description：自动删除日志，凌晨0点执行
 * time：2020/6/11  11:39
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@Component
public class AutoDeleteLogs implements InitializingBean {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(AutoDeleteLogs.class);
    private ElasticLowerClient elasticLowerClient;
    @Value("${admin.log.keepDays:0}")
    private int keepDays;
    @Value("${plumelog.es.esHosts}")
    private String esHosts;

    @Value("${plumelog.es.userName:}")
    private String userName;

    @Value("${plumelog.es.passWord:}")
    private String passWord;

    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron="0/5 * * * * *")
    public void deleteLogs() {
        if (keepDays > 0) {
            try {
                logger.info("begin delete {} days ago logs!", keepDays);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -keepDays);
                Date date = cal.getTime();
                String runLogIndex = LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + DateUtil.parseDateToStr(date, DateUtil.DATE_FORMAT_YYYYMMDD);
                elasticLowerClient.deleteIndex(runLogIndex);
                String traceLogIndex = LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + DateUtil.parseDateToStr(date, DateUtil.DATE_FORMAT_YYYYMMDD);
                elasticLowerClient.deleteIndex(traceLogIndex);
                logger.info("delete success! index:" + runLogIndex);
                logger.info("delete success! index:" + traceLogIndex);
            } catch (Exception e) {
                logger.error("delete logs error!", e);
            }
        } else {
            logger.info("unwanted delete logs");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.elasticLowerClient = ElasticLowerClient.getInstance(esHosts, userName, passWord);
    }
}
