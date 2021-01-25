package com.plumelog.agent;


import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.util.Enumeration;
import java.util.Properties;

/**
 * className：PlumelogAgent
 * description：PlumelogAgent 基类
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class PlumelogAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println(System.getProperty("user.dir"));
        Properties pps = new Properties();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("agent.properties"));
            pps.load(bufferedReader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RedisConfig.appName = pps.getProperty("appName");
        RedisConfig.redisHost = pps.getProperty("redisHost");
        RedisConfig.redisAuth = pps.getProperty("redisAuth");
        RedisConfig.redisDb = Integer.parseInt(pps.getProperty("redisDb"));
        RedisConfig.expand = pps.getProperty("expand");
        RedisConfig.runModel = pps.getProperty("runModel");
        RedisConfig.maxCount = Integer.parseInt(pps.getProperty("maxCount"));
        RedisConfig.logQueueSize = Integer.parseInt(pps.getProperty("logQueueSize"));
        RedisConfig.threadPoolSize = Integer.parseInt(pps.getProperty("threadPoolSize"));

        AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                                    TypeDescription typeDescription,
                                                    ClassLoader classLoader) {
                return builder
                        .method(ElementMatchers.nameStartsWith("initialize"))
                        .intercept(MethodDelegation.to(LogInterceptor.class));
            }
        };
        AgentBuilder.Listener listener = new AgentBuilder.Listener() {
            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, DynamicType dynamicType) {
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
            }

            @Override
            public void onError(String typeName, ClassLoader classLoader, JavaModule module, Throwable throwable) {
            }

            @Override
            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module) {
            }
        };
        new AgentBuilder
                .Default()
                .type(ElementMatchers.nameStartsWith("org.springframework.boot.logging.logback.LogbackLoggingSystem"))
                .transform(transformer)
                .with(listener)
                .installOn(inst);
        System.out.println("logAgent start!");
    }
}
