package com.plumelog.server.controller;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.core.redis.RedisClient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * className：MainController
 * description：plumelog server 增強功能：1.開放rest去隊列 1.開發接口插入隊列
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@RestController
@CrossOrigin
@PropertySource(value = "classpath:plumelog.properties", ignoreResourceNotFound = true)
public class MainController {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(MainController.class);
    @Value("${plumelog.server.redis.redisHost:127.0.0.1:6379}")
    private String redisHost;
    @Value("${plumelog.server.redis.redisPassWord:}")
    private String redisPassWord;
    @Value("${plumelog.server.model:redis}")
    private String model;

    private RedisClient redisClient;

    @RequestMapping({"/getlog", "/plumelogServer/getlog"})
    public Result getlog(Integer maxSendSize) {
        if (maxSendSize == null) {
            maxSendSize = 500;
        }
        Result result = new Result();
        try {
            getRedisClient();
            List<String> logs = redisClient.getMessage(LogMessageConstant.LOG_KEY, maxSendSize);
            if (logs != null && logs.size() > 0) {
                result.setCode(200);
                result.setMessage("get logs success!");
                result.setLogs(logs);
                return result;
            }
        } catch (Exception e) {
            result.setCode(500);
            result.setMessage("get logs error! :" + e.getMessage());
        }
        result.setCode(404);
        result.setMessage("get no logs!");
        return result;
    }

    @RequestMapping({"/sendLog", "/plumelogServer/sendLog"})
    public Result sendLog(List<String> logs) {
        Result result = new Result();
        if ("redis".equals(model)) {
            try {
                getRedisClient();
                redisClient.putMessageList(LogMessageConstant.LOG_KEY, logs);
            } catch (Exception e) {
                result.setCode(500);
                result.setMessage("send logs error! :" + e.getMessage());
            }
            result.setCode(200);
            result.setMessage("send logs! success");
        } else {
            result.setCode(500);
            result.setMessage("send logs error! rest model only support redis model");
        }
        return result;
    }

    private void getRedisClient() throws LogQueueConnectException {
        if (this.redisClient == null) {
            if (StringUtils.isEmpty(redisHost)) {
                logger.error("can not find redisHost config! please check the plumelog.properties(plumelog.server.redis.redisHost) ");
                throw new LogQueueConnectException("redis 写入失败！:can not find redisHost config");
            }
            String[] hs = redisHost.split(":");
            int port = 6379;
            String ip = "127.0.0.1";
            if (hs.length == 2) {
                ip = hs[0];
                port = Integer.valueOf(hs[1]);
            } else {
                logger.error("redis config error! please check the plumelog.properties(plumelog.server.redis.redisHost) ");
                throw new LogQueueConnectException("redis 写入失败！:redis config error");
            }
            this.redisClient = RedisClient.getInstance(ip, port, redisPassWord);
        }
    }
}
