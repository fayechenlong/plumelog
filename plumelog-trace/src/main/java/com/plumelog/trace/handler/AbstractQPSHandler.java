package com.plumelog.trace.handler;

import com.plumelog.core.lang.ShutdownHookCallback;
import com.plumelog.core.lang.ShutdownHookCallbacks;
import com.plumelog.core.util.ThreadPoolUtil;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * AbstractQPSHandler
 *
 * @Author caijian
 * @Date 2020/12/22 3:31 下午
 */
public abstract class AbstractQPSHandler {

    static ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool(4, 10, 5000, "plumelog-qps");

    static {
        ShutdownHookCallbacks.INSTANCE.addCallback(new ShutdownHookCallback() {
            @Override
            public void execute() {
                threadPoolExecutor.shutdown();
            }
        });
    }

}
