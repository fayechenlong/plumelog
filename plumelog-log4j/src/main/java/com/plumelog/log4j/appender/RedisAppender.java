package com.plumelog.log4j.appender;

import com.plumelog.core.ClientConfig;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.redis.JedisPoolRedisClient;
import com.plumelog.core.redis.RedisClientFactory;
import com.plumelog.core.redis.RedisClientHandler;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.IpGetter;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.log4j.util.LogMessageUtil;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.dto.BaseLogMessage;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：RedisAppender
 * description：RedisAppender 如果使用redis作为队列用这个RedisAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RedisAppender extends AppenderSkeleton {
    private RedisClientFactory redisClient;
    private String namespance = "plumelog";
    private String appName;
    private String redisHost;
    private String redisPort;
    private String redisAuth;
    private String redisKey;
    private int redisDb=0;
    private String runModel;
    private int maxCount=100;
    private int logQueueSize=10000;
    private int threadPoolSize=1;

    public void setNamespance(String namespance) {
        this.namespance = namespance;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }

    public void setRedisAuth(String redisAuth) {
        this.redisAuth = redisAuth;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    public void setRedisDb(int redisDb) {
        this.redisDb = redisDb;
    }

    public void setRunModel(String runModel) {
        this.runModel = runModel;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public void setLogQueueSize(int logQueueSize) {
        this.logQueueSize = logQueueSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    private static ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool();
    @Override
    protected void append(LoggingEvent loggingEvent) {
        if (this.runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(this.runModel);
        }

        ClientConfig.setNameSpance(namespance);
        ClientConfig.setAppName(appName);
        ClientConfig.setServerName(IpGetter.CURRENT_IP);

        //todo 通过配置中心，拉取redis相关配置
        if (this.redisClient == null) {
            // 启动 handler
            RedisClientHandler redisClientHandler = new RedisClientHandler();
            redisClientHandler.init(
                    new JedisPoolRedisClient(
                            this.redisHost,
                            this.redisPort == null ? LogMessageConstant.REDIS_DEFAULT_PORT : Integer.parseInt(this.redisPort),
                            this.redisAuth,
                            this.redisDb)
            );
            redisClientHandler.pullConfig();
            this.redisClient = RedisClientFactory.getInstance();

            MessageAppenderFactory.rundataQueue=new LinkedBlockingQueue<>(this.logQueueSize);
            MessageAppenderFactory.tracedataQueue=new LinkedBlockingQueue<>(this.logQueueSize);
            for(int a=0;a<this.threadPoolSize;a++){
                threadPoolExecutor.execute(()-> MessageAppenderFactory.startRunLog(this.redisClient,maxCount));

                threadPoolExecutor.execute(()-> MessageAppenderFactory.startTraceLog(this.redisClient,maxCount));
            }
        }
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(this.appName, loggingEvent);
        if (logMessage instanceof RunLogMessage) {
            final String message = LogMessageUtil.getLogMessage(logMessage, loggingEvent);
            MessageAppenderFactory.pushRundataQueue(message);
        } else {
            MessageAppenderFactory.pushTracedataQueue(GfJsonUtil.toJSONString(logMessage));
        }
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
