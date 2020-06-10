package com.plumelog.server.controller;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.server.InitConfig;
import com.plumelog.server.util.GfJsonUtil;
import org.omg.PortableInterceptor.INACTIVE;
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
 * description：MainController
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@RestController
@CrossOrigin
@PropertySource(value="classpath:plumelog.properties",ignoreResourceNotFound = true)
public class MainController {

    private  org.slf4j.Logger logger= LoggerFactory.getLogger(MainController.class);
    @Value("${plumelog.server.redis.redisHost:127.0.0.1:6379}")
    private  String redisHost;
    @Value("${plumelog.server.redis.redisPassWord:}")
    private  String redisPassWord;

    private RedisClient redisClient;

    @RequestMapping({"/getlog","/plumelogServer/getlog"})
    public Result getlog(Integer maxSendSize) {
        if(maxSendSize==null){
            maxSendSize=500;
        }
        Result result=new Result();
        try {
        if(StringUtils.isEmpty(redisHost)){
            logger.error("can not find redisHost config! please check the plumelog.properties(plumelog.server.redis.redisHost) ");
        }
        String[] hs=redisHost.split(":");
        int port=6379;
        String ip="127.0.0.1";
        if(hs.length==2){
            ip=hs[0];
            port=Integer.valueOf(hs[1]);
        }else {
            logger.error("redis config error! please check the plumelog.properties(plumelog.server.redis.redisHost) ");
        }
        if(this.redisClient==null) {
            this.redisClient = RedisClient.getInstance(ip, port, redisPassWord);
        }
            List<String> logs=redisClient.getMessage(LogMessageConstant.LOG_KEY, maxSendSize);
        if(logs!=null&&logs.size()>0) {
            result.setCode(200);
            result.setMessage("get logs success!");
            result.setLogs(logs);
            return result;
        }
        }catch (Exception e){
            result.setCode(500);
            result.setMessage("get logs error! :"+e.getMessage());
        }
        result.setCode(404);
        result.setMessage("get no logs!");
        return result;
    }
}
