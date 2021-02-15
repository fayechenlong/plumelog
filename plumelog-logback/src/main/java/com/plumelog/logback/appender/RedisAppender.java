package com.plumelog.logback.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.plumelog.core.ClientConfig;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.dto.TraceLogMessage;
import com.plumelog.core.lang.ShutdownHookCallback;
import com.plumelog.core.lang.ShutdownHookCallbacks;
import com.plumelog.core.redis.JedisPoolRedisClient;
import com.plumelog.core.redis.RedisClientFactory;
import com.plumelog.core.redis.RedisClientHandler;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.IpGetter;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.logback.util.LogMessageUtil;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：RedisAppender
 * description：
 * time：2020-05-19.15:26
 *
 * @author Tank
 * @version 1.0.0
 */
public class RedisAppender extends AppenderBase<ILoggingEvent> {
    private RedisClientFactory redisClient;
    private String namespance = "plumelog";
    private String appName;
    private String redisHost;
    private String redisPort;
    private String redisAuth;
    private int redisDb = 0;
    private String runModel;
    private String expand;
    private int maxCount = 100;
    private int logQueueSize = 10000;
    private int threadPoolSize = 1;

    public String getExpand() {
        return expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }

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

    public void setRedisDb(int redisDb) {
        this.redisDb = redisDb;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
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

    @Override
    protected void append(ILoggingEvent event) {
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, event);
        if (logMessage instanceof RunLogMessage) {
            final String message = LogMessageUtil.getLogMessage(logMessage, event);
            MessageAppenderFactory.pushRundataQueue(message);
        }else if (logMessage instanceof TraceLogMessage)  {
      /*      System.out.println("GfJsonUtil.toJSONString(logMessage)==="+GfJsonUtil.toJSONString(logMessage));
            System.out.println("logMessage.toString()==="+logMessage.toString());*/
            MessageAppenderFactory.pushTracedataQueue(logMessage.toString());
        }else
            MessageAppenderFactory.pushTracedataQueue(GfJsonUtil.toJSONString(logMessage));
    }

    ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();

    public void destroy() {
        threadPoolExecutor.shutdownNow();
    }

    @Override
    public void start() {
        super.start();

        ShutdownHookCallbacks.INSTANCE.addCallback(new ShutdownHookCallback() {
            @Override
            public void execute() {
                destroy();
            }
        });

        ClientConfig.setNameSpance(namespance);
        ClientConfig.setAppName(appName);
        ClientConfig.setServerName(IpGetter.CURRENT_IP);

        if (this.runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(this.runModel);
        }
        if (this.expand != null && LogMessageConstant.EXPANDS.contains(this.expand)) {
            LogMessageConstant.EXPAND = this.expand;
        }

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
        }
        if (MessageAppenderFactory.rundataQueue == null) {
            MessageAppenderFactory.rundataQueue = new LinkedBlockingQueue<>(this.logQueueSize);
            for (int a = 0; a < this.threadPoolSize; a++) {
                threadPoolExecutor.execute(() -> MessageAppenderFactory.startRunLog(this.redisClient, this.maxCount));
            }
        }
        if (MessageAppenderFactory.tracedataQueue == null) {
            MessageAppenderFactory.tracedataQueue = new LinkedBlockingQueue<>(this.logQueueSize);
            for (int a = 0; a < this.threadPoolSize; a++) {
                threadPoolExecutor.execute(() -> MessageAppenderFactory.startTraceLog(this.redisClient, this.maxCount));
            }
        }
    }
}
