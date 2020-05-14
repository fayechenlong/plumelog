package com.beeplay.easylog.dubbo.filter;

import com.beeplay.easylog.core.TraceId;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.springframework.util.StringUtils;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;
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
@Activate(group ={PROVIDER,CONSUMER})
public class TraceIdFilter implements Filter {

    private static final String TRACE_ID = "trace_id";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        //从本地线程中获取traceId
        String traceId = TraceId.logTraceID.get();
        //如果没有就可能是服务端那应该从Attachment获取
        if (StringUtils.isEmpty(traceId)) {
            traceId = invocation.getAttachment(TRACE_ID);
            TraceId.logTraceID.set(traceId);
        } else {
            invocation.setAttachment(TRACE_ID, traceId);
        }
        return invoker.invoke(invocation);
    }
}
