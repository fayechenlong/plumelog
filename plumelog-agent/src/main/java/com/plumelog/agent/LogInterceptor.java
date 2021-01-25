package com.plumelog.agent;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.plumelog.logback.appender.RedisAppender;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.slf4j.impl.StaticLoggerBinder;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
/**
 * className：LogInterceptor
 * description：日志配置探针拦截
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class LogInterceptor {
    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws Exception {
        try {
            return callable.call();
        } finally {
            LoggerContext lc = (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
            RedisAppender redisAppender = new RedisAppender();
            redisAppender.setAppName(RedisConfig.appName);
            redisAppender.setRedisHost(RedisConfig.redisHost);
            redisAppender.setRedisPort(RedisConfig.redisPort);
            redisAppender.setRedisAuth(RedisConfig.redisAuth);
            redisAppender.setRedisDb(RedisConfig.redisDb);
            redisAppender.setRunModel(RedisConfig.runModel);
            redisAppender.setExpand(RedisConfig.expand);
            redisAppender.setLogQueueSize(RedisConfig.logQueueSize);
            redisAppender.setMaxCount(RedisConfig.maxCount);
            redisAppender.setThreadPoolSize(RedisConfig.threadPoolSize);
            redisAppender.setContext(lc);
            redisAppender.setName("redisAppender");
            redisAppender.start();
            lc.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(redisAppender);
            System.out.println("redisAppender load success!");
        }
    }
}
