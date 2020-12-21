package com.plumelog.trace.factory;

import com.plumelog.trace.handler.QPSCalculatorHandler;

import java.util.HashMap;

/**
 * QPSCalculatorHandlerFactory
 *
 * @Author caijian
 * @Date 2020/12/17 8:05 下午
 */
public class QPSCalculatorHandlerFactory {

    private volatile static HashMap<String, QPSCalculatorHandler> handlerHashMap = new HashMap<>();

    private QPSCalculatorHandlerFactory() {
    }

    /**
     * 获取创建qps统计器
     *
     * @param requestURI
     * @return
     */
    public static QPSCalculatorHandler getHandler(String requestURI) {
        QPSCalculatorHandler handler = handlerHashMap.get(requestURI);
        if (handler == null) {
            synchronized (QPSCalculatorHandlerFactory.class) {
                if (handler == null) {
                    QPSCalculatorHandler qpsCalculatorHandler = new QPSCalculatorHandler(requestURI);
                    handlerHashMap.put(requestURI, qpsCalculatorHandler);
                    return qpsCalculatorHandler;
                }
            }
        }
        return handler;
    }

}
