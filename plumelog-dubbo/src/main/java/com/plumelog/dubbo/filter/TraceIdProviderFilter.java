package com.plumelog.dubbo.filter;

import com.plumelog.core.TraceId;
import com.plumelog.core.util.IdWorker;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.springframework.util.StringUtils;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

/**
 * 类名称：TraceIdFilter
 * 类描述：在dubbo的 filter 链中插入trace_id
 * 开发人：【Tank】
 * 创建时间：2020-05-06.18:47
 * 修改备注：
 *
 * @author Tank
 * @version 1.0.0
 */
@Activate(group = PROVIDER)
public class TraceIdProviderFilter implements Filter {
    private static final String TRACE_ID = "trace_id";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String traceId = invocation.getAttachment(TRACE_ID);
        if (StringUtils.isEmpty(traceId)) {
            IdWorker worker = new IdWorker(1, 1, 1);
            traceId = String.valueOf(worker.nextId());
        }
        TraceId.logTraceID.set(traceId);
        return invoker.invoke(invocation);
    }
}
