package com.plumelog.core.redis;

/**
 * 下线消费流程
 *
 * @author caijian
 * @version 1.0.0
 */
public interface RedisLogCollectService {

    /**
     * redis 取消注册后 消费剩余的日志
     *
     * @param redisClient
     */
    void finallyCollect(RedisClientService redisClient);


}
