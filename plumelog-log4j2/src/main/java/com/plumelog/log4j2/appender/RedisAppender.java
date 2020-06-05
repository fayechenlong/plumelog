package com.plumelog.log4j2.appender;

import com.plumelog.core.constant.LogMessageConstant;
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

    protected RedisAppender(String name, String appName, String redisHost, String redisPort,String redisAuth,String runModel, Filter filter, Layout<? extends Serializable> layout,
                            final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        this.appName = appName;
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisAuth=redisAuth;
        this.runModel=runModel;
    }

    @Override
    public void append(LogEvent logEvent) {
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(this.appName, logEvent);
        MessageAppenderFactory.push(logMessage, redisClient,"plume.log.ack");

    }

    @PluginFactory
    public static RedisAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("appName") String appName,
            @PluginAttribute("redisHost") String redisHost,
            @PluginAttribute("redisPort") String redisPort,
            @PluginAttribute("redisAuth") String redisAuth,
            @PluginAttribute("runModel") String runModel,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if(runModel!=null){
            LogMessageConstant.RUN_MODEL=Integer.parseInt(runModel);
        }
        redisClient = RedisClient.getInstance(redisHost, Integer.parseInt(redisPort), redisAuth);
        return new RedisAppender(name, appName, redisHost, redisPort,redisAuth,runModel, filter, layout, true);
    }
}
