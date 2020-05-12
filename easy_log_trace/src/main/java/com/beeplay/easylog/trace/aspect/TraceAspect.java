package com.beeplay.easylog.trace.aspect;

import com.beeplay.easylog.core.LogMessageThreadLocal;
import com.beeplay.easylog.core.TraceId;
import com.beeplay.easylog.core.TraceMessage;
import com.beeplay.easylog.core.util.LogExceptionStackTrace;
import com.beeplay.easylog.trace.dto.TraceMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
@Slf4j
public class TraceAspect {

    @Around("@annotation(com.beeplay.easylog.trace.annotation.Trace))")
    public void around(JoinPoint joinPoint) {
        TraceMessage traceMessage = LogMessageThreadLocal.logMessageThreadLocal.get();
        if (traceMessage == null) {
            traceMessage = new TraceMessage();
        }
        String traceId = TraceId.logTraceID.get();
        traceMessage.setTraceId(traceId);
        traceMessage.setMessageType(joinPoint.getSignature().toString());
        LogMessageThreadLocal.logMessageThreadLocal.set(traceMessage);
        try {
            TraceMessageDTO dto = new TraceMessageDTO();
            dto.setMethod(joinPoint.getSignature().toString());
            dto.setTime(System.currentTimeMillis());
            dto.setPosition("<");
            log.info(dto.toString());
            ((ProceedingJoinPoint) joinPoint).proceed();
            dto.setTime(System.currentTimeMillis());
            dto.setPosition(">");
            log.info(dto.toString());
        } catch (
                Throwable e) {
            log.error("TID:{} , 链路：{},异常：{}", traceId, joinPoint.getSignature(), LogExceptionStackTrace.erroStackTrace(e).toString());
        }
    }
}
