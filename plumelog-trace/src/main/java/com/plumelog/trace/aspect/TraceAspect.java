package com.plumelog.trace.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * className：TraceAspect
 * description：
 * time：2020-05-09.14:34
 *
 * @author Tank
 * @version 1.0.0
 */
@Aspect
@Component
@ConditionalOnMissingBean(value = AbstractAspect.class, ignored = TraceAspect.class)
public class TraceAspect extends AbstractAspect {
    @Around("@annotation(com.plumelog.trace.annotation.Trace))")
    public Object around(JoinPoint joinPoint) throws Throwable {
        return aroundExecute(joinPoint);
    }

}
