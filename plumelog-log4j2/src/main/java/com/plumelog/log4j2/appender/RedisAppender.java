package com.plumelog.log4j2.appender;

import com.plumelog.core.ClientConfig;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.redis.RedisClientFactory;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.core.util.IpGetter;
import com.plumelog.core.util.ThreadPoolUtil;
import com.plumelog.log4j2.util.LogMessageUtil;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.dto.BaseLogMessage;
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
    private static RedisClientFactory redisClient;
    private String namespance = "plumelog";
    private String appName;
    private String redisHost;
    private String redisPort;
    private String redisAuth;
    private String runModel;
    private String expand;
    private int redisDb=0;
    private int maxCount=500;
    private int logQueueSize=10000;
    private int threadPoolSize=5;

    protected RedisAppender(String name, String namespance, String appName, String redisHost, String redisPort,String redisAuth,String runModel, Filter filter, Layout<? extends Serializable> layout,
                            final boolean ignoreExceptions,String expand,int maxCount,int logQueueSize,int redisDb,int threadPoolSize) {
        super(name, filter, layout, ignoreExceptions);
        this.namespance = namespance;
        this.appName = appName;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisAuth=redisAuth;
        this.runModel=runModel;
        this.expand = expand;
        this.maxCount=maxCount;
        this.logQueueSize=logQueueSize;
        this.redisDb=redisDb;
        this.threadPoolSize=threadPoolSize;
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
            @PluginAttribute("namespance") String namespance,
            @PluginAttribute("appName") String appName,
            @PluginAttribute("redisHost") String redisHost,
            @PluginAttribute("redisPort") String redisPort,
            @PluginAttribute("redisAuth") String redisAuth,
            @PluginAttribute("maxCount") int maxCount,
            @PluginAttribute("runModel") String runModel,
            @PluginAttribute("expand") String expand,
            @PluginAttribute("redisDb") int redisDb,
            @PluginAttribute("logQueueSize") int logQueueSize,
            @PluginAttribute("threadPoolSize") int threadPoolSize,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if(runModel!=null){
            LogMessageConstant.RUN_MODEL=Integer.parseInt(runModel);
        }

        ClientConfig.setNameSpance(namespance);
        ClientConfig.setAppName(appName);
        ClientConfig.setServerName(IpGetter.CURRENT_IP);

        if (expand != null && LogMessageConstant.EXPANDS.contains(expand)) {
            LogMessageConstant.EXPAND = expand;
        }
        redisClient = RedisClientFactory.getInstance(redisHost, redisPort == null ?
                LogMessageConstant.REDIS_DEFAULT_PORT
                : Integer.parseInt(redisPort), redisAuth,redisDb);
        if(maxCount==0){
            maxCount=100;
        }
        if(logQueueSize==0){
            logQueueSize=10000;
        }
        if(threadPoolSize==0){
            threadPoolSize=1;
        }
        final int count=maxCount;
        MessageAppenderFactory.rundataQueue=new LinkedBlockingQueue<>(logQueueSize);
        MessageAppenderFactory.tracedataQueue=new LinkedBlockingQueue<>(logQueueSize);
        for(int a=0;a<threadPoolSize;a++){
            threadPoolExecutor.execute(()->{
                MessageAppenderFactory.startRunLog(redisClient,count);
            });
            threadPoolExecutor.execute(()->{
                MessageAppenderFactory.startTraceLog(redisClient,count);
            });
        }
        return new RedisAppender(name, namespance, appName, redisHost, redisPort,redisAuth,runModel, filter, layout, true,expand,maxCount,logQueueSize,redisDb,threadPoolSize);
    }
}
