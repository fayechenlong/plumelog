package com.plumelog.log4j2.appender;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
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
 * className：LiteAppender
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@Plugin(name = "LiteAppender", category = "Core", elementType = "appender", printObject = true)
public class LiteAppender extends AbstractAppender {
    private static AbstractClient redisClient;
    private static final ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool();
    private final String appName;
    private final String env;
    private final String plumelogHost;
    private final String runModel;
    private final String expand;
    private int maxCount = 500;
    private int logQueueSize = 10000;
    private int threadPoolSize = 5;
    private boolean compressor = false;
    private String model = "standalone";

    protected LiteAppender(String name, String appName, String env, String plumelogHost, String runModel, Filter filter, Layout<? extends Serializable> layout,
                           final boolean ignoreExceptions, String expand, int maxCount, int logQueueSize, int threadPoolSize, boolean compressor, String model, String masterName) {
        super(name, filter, layout, ignoreExceptions);
        this.appName = appName;
        this.env = env;
        this.plumelogHost = plumelogHost;
        this.runModel = runModel;
        this.expand = expand;
        this.maxCount = maxCount;
        this.logQueueSize = logQueueSize;
        this.threadPoolSize = threadPoolSize;
        this.compressor = compressor;
        this.model = model;
    }

    @PluginFactory
    public static LiteAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("appName") String appName,
            @PluginAttribute("env") String env,
            @PluginAttribute("plumelogHost") String plumelogHost,
            @PluginAttribute("maxCount") int maxCount,
            @PluginAttribute("runModel") String runModel,
            @PluginAttribute("expand") String expand,
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
                MessageAppenderFactory.startRunLog(plumelogHost, count,
                        compressor ? LogMessageConstant.LOG_KEY_COMPRESS : LogMessageConstant.LOG_KEY, compressor);
            });
            threadPoolExecutor.execute(() -> {
                MessageAppenderFactory.startTraceLog(plumelogHost, count,
                        compressor ? LogMessageConstant.LOG_KEY_TRACE_COMPRESS : LogMessageConstant.LOG_KEY_TRACE, compressor);
            });
        }
        return new LiteAppender(name, appName, env, plumelogHost, runModel, filter, layout, true, expand, maxCount, logQueueSize, threadPoolSize, compressor, model, masterName);
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

    public String getPlumelogHost() {
        return plumelogHost;
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
