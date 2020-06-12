package com.plumelog.server.collect;


import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.server.InitConfig;
import com.plumelog.server.client.ElasticLowerClient;
import com.plumelog.server.client.PlumeRestClient;
import com.plumelog.server.util.DateUtil;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * className：RedisLogCollect
 * description：RedisLogCollect 获取rest接口中日志，存储到es
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RestLogCollect extends BaseLogCollect {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(RestLogCollect.class);
    private String restUserName = "";
    private String restPassWord = "";
    private String restUrl = "";

    /**
     * 无密码redis
     *
     * @param restUrl
     * @param esHosts
     * @param userName
     * @param passWord
     */
    public RestLogCollect(String restUrl, String esHosts, String userName, String passWord, String restUserName, String restPassWord) {

        this.restUserName = restUserName;
        this.restPassWord = restPassWord;
        this.restUrl = restUrl;
        super.elasticLowerClient = ElasticLowerClient.getInstance(esHosts, userName, passWord);
        logger.info("elasticSearch init success!esHosts:{}", esHosts);
        logger.info("restUrl:{}", restUrl);
    }


    public void restStart() {

        threadPoolExecutor.execute(() -> {
            collectRuningLog();
        });
        threadPoolExecutor.execute(() -> {
            collectTraceLog();
        });
        logger.info("RestLogCollect is starting!");
    }

    private void collectRuningLog() {
        while (true) {
            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
                List<String> logs = PlumeRestClient.getLogs(this.restUrl + "?maxSendSize=" + InitConfig.MAX_SEND_SIZE + "&logKey=" + LogMessageConstant.LOG_KEY, this.restUserName, this.restPassWord);
                collect(logs, LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD));
            } catch (InterruptedException e) {
                logger.error("", e);
            } catch (Exception e) {
                logger.error("从plumelog-server拉取日志失败！", e);
            }
        }
    }

    private void collectTraceLog() {
        while (true) {
            try {
                Thread.sleep(InitConfig.MAX_INTERVAL);
                List<String> logs = PlumeRestClient.getLogs(this.restUrl + "?maxSendSize=" + InitConfig.MAX_SEND_SIZE + "&logKey=" + LogMessageConstant.LOG_KEY_TRACE, this.restUserName, this.restPassWord);
                collectTrace(logs, LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + DateUtil.parseDateToStr(new Date(), DateUtil.DATE_FORMAT_YYYYMMDD));
            } catch (InterruptedException e) {
                logger.error("", e);
            } catch (Exception e) {
                logger.error("从plumelog-server队列拉取日志失败！", e);
            }
        }
    }

    private void collect(List<String> logs, String index) {
        if (logs.size() > 0) {
            logs.forEach(log -> {
                logger.debug("get log:" + log);
                super.logList.add(log);
            });
            if (super.logList.size() > 0) {
                List<String> logList = new ArrayList();
                logList.addAll(super.logList);
                super.logList.clear();
                super.sendLog(index, logList);
            }
        }
    }

    private void collectTrace(List<String> logs, String index) {
        if (logs.size() > 0) {
            logs.forEach(log -> {
                logger.debug("get log:" + log);
                super.traceLogList.add(log);
            });
            if (super.traceLogList.size() > 0) {
                List<String> logList = new ArrayList();
                logList.addAll(super.traceLogList);
                super.traceLogList.clear();
                super.sendTraceLogList(index, logList);
            }
        }
    }
}
