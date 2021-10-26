package com.plumelog.log4j2.appender;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.core.redis.RedisClusterClient;
import com.plumelog.core.redis.RedisSentinelClient;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.log4j2.util.LogMessageUtil;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：RedisAppender
 * description：RedisAppender 如果使用redis作为队列用这个RedisAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@Plugin(name = "RedisAppender", category = "Core", elementType = "appender", printObject = true)
public class RedisAppender extends AbstractAppender {
    private static AbstractClient redisClient;
    private static final ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool();
    private final String appName;
    private final String env;
    private final String redisHost;
    private final String redisPort;
    private final String redisAuth;
    private final String runModel;
    private final String expand;
    private int redisDb = 0;
    private int maxCount = 500;
    private int logQueueSize = 10000;
    private int threadPoolSize = 5;
    private boolean compressor = false;
    private String model = "standalone";
    private final String masterName;

    protected RedisAppender(String name, String appName, String env, String redisHost, String redisPort, String redisAuth, String runModel, Filter filter, Layout<? extends Serializable> layout,
                            final boolean ignoreExceptions, String expand, int maxCount, int logQueueSize, int redisDb, int threadPoolSize, boolean compressor, String model, String masterName) {
        super(name, filter, layout, ignoreExceptions);
        this.appName = appName;
        this.env = env;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisAuth = redisAuth;
        this.runModel = runModel;
        this.expand = expand;
        this.maxCount = maxCount;
        this.logQueueSize = logQueueSize;
        this.redisDb = redisDb;
        this.threadPoolSize = threadPoolSize;
        this.compressor = compressor;
        this.model = model;
        this.masterName = masterName;
    }

    @PluginFactory
    public static RedisAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("appName") String appName,
            @PluginAttribute("env") String env,
            @PluginAttribute("redisHost") String redisHost,
            @PluginAttribute("redisPort") String redisPort,
            @PluginAttribute("redisAuth") String redisAuth,
            @PluginAttribute("maxCount") int maxCount,
            @PluginAttribute("runModel") String runModel,
            @PluginAttribute("expand") String expand,
            @PluginAttribute("redisDb") int redisDb,
            @PluginAttribute("logQueueSize") int logQueueSize,
            @PluginAttribute("threadPoolSize") int threadPoolSize,
            @PluginAttribute("compressor") boolean compressor,
            @PluginAttribute("model") String model,
            @PluginAttribute("masterName") String masterName,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if (env == null) {
            env = "default";
        }
        if (runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(runModel);
        }
        if (expand != null && LogMessageConstant.EXPANDS.contains(expand)) {
            LogMessageConstant.EXPAND = expand;
        }
        if (model == null) {
            model = "standalone";
        }
        if ("sentinel".equals(model)) {
            redisClient = RedisSentinelClient.getInstance(redisHost, masterName, redisAuth, redisDb);
        } else {
            int port = 6379;
            String ip = "127.0.0.1";
            if (redisPort == null) {
                // 如果redisHost不包含:号则端口号默认使用6379
                if (redisHost.contains(":")) {
                    String[] hs = redisHost.split(":");
                    if (hs.length == 2) {
                        ip = hs[0];
                        port = Integer.parseInt(hs[1]);
                    }
                } else {
                    ip = redisHost;
                }
            } else {
                ip = redisHost;
                port = Integer.parseInt(redisPort);
            }
            redisClient = RedisClient.getInstance(ip, port, redisAuth, redisDb);
        }
        if (maxCount == 0) {
            maxCount = 100;
        }
        if (logQueueSize == 0) {
            logQueueSize = 10000;
        }
        if (threadPoolSize == 0) {
            threadPoolSize = 1;
        }
        final int count = maxCount;
        MessageAppenderFactory.initQueue(logQueueSize);
        for (int a = 0; a < threadPoolSize; a++) {
            threadPoolExecutor.execute(() -> {
                MessageAppenderFactory.startRunLog(redisClient, count,
                        compressor ? LogMessageConstant.LOG_KEY_COMPRESS : LogMessageConstant.LOG_KEY, compressor);
            });
            threadPoolExecutor.execute(() -> {
                MessageAppenderFactory.startTraceLog(redisClient, count,
                        compressor ? LogMessageConstant.LOG_KEY_TRACE_COMPRESS : LogMessageConstant.LOG_KEY_TRACE, compressor);
            });
        }
        return new RedisAppender(name, appName, env, redisHost, redisPort, redisAuth, runModel, filter, layout, true, expand, maxCount, logQueueSize, redisDb, threadPoolSize, compressor, model, masterName);
    }

    @Override
    public void append(LogEvent logEvent) {
        if (logEvent != null) {
            send(logEvent);
        }
    }

    protected void send(LogEvent logEvent) {
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, env, logEvent);
        if (logMessage instanceof RunLogMessage) {
            final String message = LogMessageUtil.getLogMessage(logMessage, logEvent);
            MessageAppenderFactory.pushRundataQueue(message);
        } else {
            MessageAppenderFactory.pushTracedataQueue(GfJsonUtil.toJSONString(logMessage));
        }
    }

    public String getAppName() {
        return appName;
    }

    public String getModel() {
        return model;
    }

    public String getRunModel() {
        return runModel;
    }

    public String getMasterName() {
        return masterName;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public String getRedisPort() {
        return redisPort;
    }

    public int getRedisDb() {
        return redisDb;
    }

    public String getRedisAuth() {
        return redisAuth;
    }

    public String getExpand() {
        return expand;
    }

    public String getEnv() {
        return env;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public int getLogQueueSize() {
        return logQueueSize;
    }

    public boolean isCompressor() {
        return compressor;
    }
}
