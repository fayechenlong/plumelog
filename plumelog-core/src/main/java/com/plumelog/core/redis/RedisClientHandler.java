package com.plumelog.core.redis;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * RedisClientHandler
 *
 * @Author caijian
 * @Date 2020/12/28 6:12 下午
 */
public class RedisClientHandler {


    //todo 同步config信息
    public static void pullConfig() {
        ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(1);
        scheduled.scheduleWithFixedDelay(() -> {

            // 获取redis


            // 注册redis client




        }, 1, 30, TimeUnit.SECONDS);


    }


}
