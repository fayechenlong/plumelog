package com.plumelog.agent;


import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;

public class PlumelogAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
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
        System.out.println("logagent start!");
    }
}
