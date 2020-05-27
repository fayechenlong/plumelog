package com.beeplay.easylog.log4j2.appender;

import com.beeplay.easylog.core.MessageAppenderFactory;
import com.beeplay.easylog.core.dto.BaseLogMessage;
import com.beeplay.easylog.core.redis.RedisClient;
import com.beeplay.easylog.log4j2.util.LogMessageUtil;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;

@Plugin(name = "RedisAppender", category = "Core", elementType = "appender", printObject = true)
public class RedisAppender extends AbstractAppender {
    private static RedisClient redisClient;
    private String appName;
    private String reidsHost;
    private String redisPort;
    private String redisAuth;

    protected RedisAppender(String name, String appName, String reidsHost, String redisPort,String redisAuth, Filter filter, Layout<? extends Serializable> layout,
                            final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        this.appName = appName;
        this.reidsHost = reidsHost;
        this.redisPort = redisPort;
        this.redisAuth=redisAuth;
    }

    @Override
    public void append(LogEvent logEvent) {
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(this.appName, logEvent);
        MessageAppenderFactory.push(logMessage, redisClient);

    }

    @PluginFactory
    public static RedisAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("appName") String appName,
            @PluginAttribute("reidsHost") String reidsHost,
            @PluginAttribute("redisPort") String redisPort,
            @PluginAttribute("redisAuth") String redisAuth,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        redisClient = RedisClient.getInstance(reidsHost, Integer.parseInt(redisPort), redisAuth);
        return new RedisAppender(name, appName, reidsHost, redisPort,redisAuth, filter, layout, true);
    }
}
