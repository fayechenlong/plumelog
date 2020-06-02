package com.plumelog.dubbo.filter;

import com.plumelog.core.TraceId;
import com.plumelog.core.util.IdWorker;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.springframework.util.StringUtils;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;

/**
 * @ClassName TraceIdFilter1
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/19 11:07
 * @Version 1.0
 **/
@Activate(group =CONSUMER)
public class TraceIdConsumerFilter implements Filter {
    private static final String TRACE_ID = "trace_id";
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String traceId =TraceId.logTraceID.get();
        if (StringUtils.isEmpty(traceId)) {
            IdWorker worker = new IdWorker(1,1,1);
            traceId=String.valueOf(worker.nextId());
            TraceId.logTraceID.set(traceId);
        }
        invocation.setAttachment(TRACE_ID,traceId);
        return invoker.invoke(invocation);
    }
}
