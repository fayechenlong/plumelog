package com.beeplay.easylog.trace.aspect;

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
@ConditionalOnMissingBean(AbstractAspect.class)
public class TraceAspect extends AbstractAspect {
    @Around("@annotation(com.beeplay.easylog.trace.annotation.Trace))")
    public Object around(JoinPoint joinPoint) {
        return aroundExecute(joinPoint);
    }
}
