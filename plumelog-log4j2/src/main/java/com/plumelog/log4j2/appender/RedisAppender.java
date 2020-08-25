package com.plumelog.log4j2.appender;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.log4j2.util.LogMessageUtil;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.redis.RedisClient;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
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
    private static RedisClient redisClient;
    private String appName;
    private String redisHost;
    private String redisPort;
    private String redisAuth;
    private String runModel;
    private String expand;
    private int maxCount=500;
    private int logQueueSize=10000;

    protected RedisAppender(String name, String appName, String redisHost, String redisPort,String redisAuth,String runModel, Filter filter, Layout<? extends Serializable> layout,
                            final boolean ignoreExceptions,String expand,int maxCount,int logQueueSize) {
        super(name, filter, layout, ignoreExceptions);
        this.appName = appName;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisAuth=redisAuth;
        this.runModel=runModel;
        this.expand = expand;
        this.maxCount=maxCount;
        this.logQueueSize=logQueueSize;
    }

    @Override
    public void append(LogEvent logEvent) {
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, logEvent);
        if (logMessage instanceof RunLogMessage) {
            final String message = LogMessageUtil.getLogMessage(logMessage, logEvent);
            MessageAppenderFactory.pushRundataQueue(message);
        } else {
            MessageAppenderFactory.pushTracedataQueue(GfJsonUtil.toJSONString(logMessage));
        }
    }
    private static ThreadPoolExecutor threadPoolExecutor
            = ThreadPoolUtil.getPool();
    @PluginFactory
    public static RedisAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("appName") String appName,
            @PluginAttribute("redisHost") String redisHost,
            @PluginAttribute("redisPort") String redisPort,
            @PluginAttribute("redisAuth") String redisAuth,
            @PluginAttribute("runModel") String runModel,
            @PluginAttribute("expand") String expand,
            @PluginAttribute("maxCount") int maxCount,
            @PluginAttribute("logQueueSize") int logQueueSize,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if(runModel!=null){
            LogMessageConstant.RUN_MODEL=Integer.parseInt(runModel);
        }
        if (expand != null && LogMessageConstant.EXPANDS.contains(expand)) {
            LogMessageConstant.EXPAND = expand;
        }
        redisClient = RedisClient.getInstance(redisHost, redisPort == null ?
                LogMessageConstant.REDIS_DEFAULT_PORT
                : Integer.parseInt(redisPort), redisAuth);
        if(maxCount==0){
            maxCount=100;
        }
        if(logQueueSize==0){
            logQueueSize=10000;
        }
        final int queueSize=logQueueSize;
        final int count=maxCount;
        for(int a=0;a<5;a++){

            threadPoolExecutor.execute(()->{
                MessageAppenderFactory.rundataQueue=new LinkedBlockingQueue<>(queueSize);
                MessageAppenderFactory.startRunLog(redisClient,count);
            });
            threadPoolExecutor.execute(()->{
                MessageAppenderFactory.tracedataQueue=new LinkedBlockingQueue<>(queueSize);
                MessageAppenderFactory.startTraceLog(redisClient,count);
            });
        }
        return new RedisAppender(name, appName, redisHost, redisPort,redisAuth,runModel, filter, layout, true,expand,maxCount,logQueueSize);
    }
}
