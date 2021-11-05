package com.plumelog.log4j.appender;

import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.log4j.util.LogMessageUtil;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * className：LiteAppender
 * description：
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class LiteAppender extends AppenderSkeleton {
    private static final AtomicBoolean INIT = new AtomicBoolean();
    private static final ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    private String appName;
    private String env = "default";
    private String runModel;
    private int maxCount = 100;
    private int logQueueSize = 10000;
    private int threadPoolSize = 1;
    private boolean compressor = false;
    private String model = "standalone";
    private String plumelogHost;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }


    public String getRunModel() {
        return runModel;
    }

    public void setRunModel(String runModel) {
        this.runModel = runModel;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getLogQueueSize() {
        return logQueueSize;
    }

    public void setLogQueueSize(int logQueueSize) {
        this.logQueueSize = logQueueSize;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public boolean isCompressor() {
        return compressor;
    }

    public void setCompressor(boolean compressor) {
        this.compressor = compressor;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPlumelogHost() {
        return plumelogHost;
    }

    public void setPlumelogHost(String plumelogHost) {
        this.plumelogHost = plumelogHost;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if (this.runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(this.runModel);
        }
        // 当多并发情况下若redisClient为空时会多次执行以下代码，因此增加是否已初始化的判断
        if (INIT.compareAndSet(false, true)) {
            for (int a = 0; a < this.threadPoolSize; a++) {

                threadPoolExecutor.execute(() -> {
                    MessageAppenderFactory.startRunLog(this.plumelogHost, maxCount,
                            this.compressor ? LogMessageConstant.LOG_KEY_COMPRESS : LogMessageConstant.LOG_KEY, this.compressor);
                });
                threadPoolExecutor.execute(() -> {
                    MessageAppenderFactory.startTraceLog(this.plumelogHost, maxCount,
                            this.compressor ? LogMessageConstant.LOG_KEY_TRACE_COMPRESS : LogMessageConstant.LOG_KEY_TRACE, this.compressor);
                });
            }
        }
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(this.appName, this.env, loggingEvent);
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
