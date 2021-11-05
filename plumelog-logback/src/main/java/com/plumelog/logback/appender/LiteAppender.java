package com.plumelog.logback.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.logback.util.LogMessageUtil;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：LiteAppender
 * description：
 * time：2021-11-01
 *
 * @author chenlongfei
 * @version 1.0.0
 */
public class LiteAppender extends AppenderBase<ILoggingEvent> {
    private static final ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    private String appName;
    private String env = "default";
    private String runModel;
    private String expand;
    private String plumelogHost;
    private int maxCount = 100;
    private int logQueueSize = 10000;
    private int threadPoolSize = 1;
    private boolean compressor = false;

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

    public String getPlumelogHost() {
        return plumelogHost;
    }

    public void setPlumelogHost(String plumelogHost) {
        this.plumelogHost = plumelogHost;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getRunModel() {
        return runModel;
    }

    public void setRunModel(String runModel) {
        this.runModel = runModel;
    }

    public String getExpand() {
        return expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (event != null) {
            send(event);
        }
    }

    protected void send(ILoggingEvent event) {
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, env, event);
        if (logMessage instanceof RunLogMessage) {
            final String message = LogMessageUtil.getLogMessage(logMessage, event);

            MessageAppenderFactory.pushRundataQueue(message);
        } else {
            MessageAppenderFactory.pushTracedataQueue(GfJsonUtil.toJSONString(logMessage));

        }
    }

    @Override
    public void start() {
        super.start();
        if (this.runModel != null) {
            LogMessageConstant.RUN_MODEL = Integer.parseInt(this.runModel);
        }
        if (this.expand != null && LogMessageConstant.EXPANDS.contains(this.expand)) {
            LogMessageConstant.EXPAND = this.expand;
        }

        MessageAppenderFactory.initQueue(this.logQueueSize);
        for (int a = 0; a < this.threadPoolSize; a++) {
            threadPoolExecutor.execute(() -> MessageAppenderFactory.startRunLog(this.plumelogHost, this.maxCount,
                    this.compressor ? LogMessageConstant.LOG_KEY_COMPRESS : LogMessageConstant.LOG_KEY, this.compressor));
        }
        for (int a = 0; a < this.threadPoolSize; a++) {
            threadPoolExecutor.execute(() -> MessageAppenderFactory.startTraceLog(this.plumelogHost, this.maxCount,
                    this.compressor ? LogMessageConstant.LOG_KEY_TRACE_COMPRESS : LogMessageConstant.LOG_KEY_TRACE, this.compressor));
        }
    }
}
