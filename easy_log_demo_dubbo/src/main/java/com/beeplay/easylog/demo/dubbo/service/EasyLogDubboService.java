package com.beeplay.easylog.demo.dubbo.service;

import com.beeplay.easylog.trace.annotation.Trace;

/**
 * @InterfaceName EasyLogDubboService
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/13 16:04
 * @Version 1.0
 **/
public interface EasyLogDubboService {

    @Trace
    void testLogDubbo();
}
