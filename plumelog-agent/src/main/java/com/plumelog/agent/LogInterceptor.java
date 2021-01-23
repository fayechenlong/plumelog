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

public class LogInterceptor {
    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws Exception {
        try {
            return callable.call();
        } finally {
            LoggerContext lc = (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
            RedisAppender redisAppender = new RedisAppender();
            redisAppender.setAppName("agent");
            redisAppender.setRedisHost("10.100.2.54");
            redisAppender.setRedisPort("6379");
            redisAppender.setRedisAuth("plumelogredis");
            redisAppender.setContext(lc);
            redisAppender.setName("redisAppender");
            redisAppender.start();
            lc.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(redisAppender);
            System.out.println("初始化日志");
        }
    }
}
