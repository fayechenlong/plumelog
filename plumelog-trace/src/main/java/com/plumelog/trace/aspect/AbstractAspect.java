package com.plumelog.trace.aspect;

import com.plumelog.core.LogMessageThreadLocal;
import com.plumelog.core.TraceId;
import com.plumelog.core.TraceMessage;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.util.GfJsonUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.LoggerFactory;

/**
 * className：AbstractAspect
 * description： 链路追踪打点拦截
 * time：2020-05-26.11:17
 *
 * @author Tank,fran.chen
 * @version 1.2.0
 */
public abstract class AbstractAspect {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(AbstractAspect.class);

    public Object aroundExecute(JoinPoint joinPoint) throws Throwable {
        TraceMessage traceMessage = LogMessageThreadLocal.logMessageThreadLocal.get();
        String traceId = TraceId.logTraceID.get();
        if (traceMessage == null) {
            traceMessage = new TraceMessage();
            traceMessage.getPositionNum().set(0);
        }
        traceMessage.setMessageType(joinPoint.getSignature().toString());
        traceMessage.setPosition(LogMessageConstant.TRACE_START);
        traceMessage.getPositionNum().incrementAndGet();
        LogMessageThreadLocal.logMessageThreadLocal.set(traceMessage);
        if (traceId != null) {
            log.info(LogMessageConstant.TRACE_PRE + GfJsonUtil.toJSONString(traceMessage));
        }
        Object proceed = ((ProceedingJoinPoint) joinPoint).proceed();
        traceMessage.setMessageType(joinPoint.getSignature().toString());
        traceMessage.setPosition(LogMessageConstant.TRACE_END);
        traceMessage.getPositionNum().incrementAndGet();
        if (traceId != null) {
            log.info(LogMessageConstant.TRACE_PRE + GfJsonUtil.toJSONString(traceMessage));
        }
        return proceed;
    }
}
