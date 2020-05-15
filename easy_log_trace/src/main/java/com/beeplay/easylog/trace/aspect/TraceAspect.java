package com.beeplay.easylog.trace.aspect;

import com.beeplay.easylog.core.LogMessageThreadLocal;
import com.beeplay.easylog.core.TraceId;
import com.beeplay.easylog.core.TraceMessage;
import com.beeplay.easylog.core.constant.LogMessageConstant;
import com.beeplay.easylog.core.util.GfJsonUtil;
import com.beeplay.easylog.core.util.LogExceptionStackTrace;
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
    public Object around(JoinPoint joinPoint) {
        TraceMessage traceMessage = LogMessageThreadLocal.logMessageThreadLocal.get();
        if (traceMessage == null) {
            traceMessage = new TraceMessage();
            traceMessage.getPositionNum().set(0);
        }
        String traceId = TraceId.logTraceID.get();
        traceMessage.setTraceId(traceId);
        traceMessage.setMessageType(joinPoint.getSignature().toString());
        traceMessage.setPosition(LogMessageConstant.TRACE_START);
        traceMessage.getPositionNum().incrementAndGet();
        LogMessageThreadLocal.logMessageThreadLocal.set(traceMessage);
        Object proceed = null;
        try {
            log.info(LogMessageConstant.TRACE_PRE + GfJsonUtil.toJSONString(traceMessage));
            proceed = ((ProceedingJoinPoint) joinPoint).proceed();
            traceMessage.setMessageType(joinPoint.getSignature().toString());
            traceMessage.setPosition(LogMessageConstant.TRACE_END);
            traceMessage.getPositionNum().incrementAndGet();
            log.info(LogMessageConstant.TRACE_PRE + GfJsonUtil.toJSONString(traceMessage));
        } catch (Throwable e) {
            log.error("TID:{} , 链路：{},异常：{}", traceId, joinPoint.getSignature(),
                    LogExceptionStackTrace.erroStackTrace(e).toString());
        }
        return proceed;
    }
}
