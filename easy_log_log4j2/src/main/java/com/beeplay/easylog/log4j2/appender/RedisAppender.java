package com.beeplay.easylog.log4j2.appender;

import com.beeplay.easylog.core.LogMessage;
import com.beeplay.easylog.core.redis.RedisClient;
import com.beeplay.easylog.core.util.GfJsonUtil;
import com.beeplay.easylog.core.util.ThreadPoolUtil;
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
import java.util.concurrent.ThreadPoolExecutor;

@Plugin(name = "RedisAppender", category = "Core", elementType = "appender", printObject = true)
public class RedisAppender extends AbstractAppender {
    private ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    private RedisClient redisClient;
    private String appName;
    private String reidsHost;
    private String redisPort;
    private String redisKey;

    protected RedisAppender(String name, String appName, String reidsHost, String redisPort,String redisKey, Filter filter, Layout<? extends Serializable> layout,
                            final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        this.appName = appName;
        this.reidsHost = reidsHost;
        this.redisPort = redisPort;
        this.redisKey = redisKey;
    }
    @Override
    public void append(LogEvent logEvent) {
        if(redisClient==null){
            redisClient = RedisClient.getInstance(this.reidsHost, Integer.parseInt(this.redisPort),"");
        }
        final String redisKey=this.redisKey;
        final LogMessage logMessage = LogMessageUtil.getLogMessage(this.appName,logEvent);
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                redisClient.pushMessage(redisKey, GfJsonUtil.toJSONString(logMessage));
            }
        });
    }
    @PluginFactory
    public static RedisAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("appName") String appName,
            @PluginAttribute("reidsHost") String reidsHost,
            @PluginAttribute("redisPort") String redisPort,
            @PluginAttribute("redisKey") String redisKey,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        return new RedisAppender(name, appName,reidsHost, redisPort,redisKey,filter, layout, true);
    }
}
