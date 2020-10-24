package com.plumelog.log4j.appender;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.TraceIdGenerator;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.StringUtil;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.log4j.util.LogMessageUtil;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：PlumelogAppender
 * description：Plumelog Base log4j Appender
 *
 * @version 1.0.0
 */
public abstract class PlumelogAppender extends AppenderSkeleton {
    protected AbstractClient plumelogClient;
    protected String appName;
    protected String srvName;
    protected String runModel;
    protected int maxCount = 100;
    protected int logQueueSize = 10000;
    protected int threadPoolSize = 1;

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setSrvName(String srvName) {
        this.srvName = srvName;
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
        if (this.plumelogClient == null) {
            initializeClient();

            MessageAppenderFactory.rundataQueue = new LinkedBlockingQueue<String>(this.logQueueSize);
            MessageAppenderFactory.tracedataQueue = new LinkedBlockingQueue<String>(this.logQueueSize);
            for (int a = 0; a < this.threadPoolSize; a++) {

                threadPoolExecutor.execute(new RunLogAppender(plumelogClient, maxCount));
                threadPoolExecutor.execute(new TraceLogAppender(plumelogClient, maxCount));
            }
        }
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(this.appName, getServerName(), loggingEvent);
        if (logMessage instanceof RunLogMessage) {
            final String message = LogMessageUtil.getLogMessage(logMessage, loggingEvent);
            MessageAppenderFactory.pushRundataQueue(message);
        } else {
            MessageAppenderFactory.pushTracedataQueue(GfJsonUtil.toJSONString(logMessage));
        }
    }

    private String getServerName() {
        if (StringUtil.isNotBlank(this.srvName)) {
            return this.srvName;
        }
        this.srvName = TraceIdGenerator.getInetAddress();
        if (StringUtil.isBlank(this.srvName)) {
            this.srvName = "";
        }
        return this.srvName;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    abstract protected void initializeClient();

    public class RunLogAppender implements Runnable {
        private AbstractClient plumelogClient;
        private int maxCount = 100;

        public RunLogAppender(AbstractClient plumelogClient, int maxCount) {
            this.plumelogClient = plumelogClient;
            this.maxCount = maxCount;
        }

        @Override
        public void run() {
            MessageAppenderFactory.startRunLog(plumelogClient, maxCount);
        }
    }

    public class TraceLogAppender implements Runnable {
        private AbstractClient plumelogClient;
        private int maxCount = 100;

        public TraceLogAppender(AbstractClient plumelogClient, int maxCount) {
            this.plumelogClient = plumelogClient;
            this.maxCount = maxCount;
        }

        @Override
        public void run() {
            MessageAppenderFactory.startTraceLog(plumelogClient, maxCount);
        }
    }
}
