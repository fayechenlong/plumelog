package com.plumelog.core.redis;

import com.plumelog.core.exception.LogQueueConnectException;

import java.util.*;

public interface RedisClientService {


    int getWeight();

    void setWeight(int weight);

    long getLatestPullTime();

    void setLatestPullTime(long latestPullTime);

    void shutdown();

    void pushMessage(String key, String strings) throws LogQueueConnectException;

    void putMessageList(String key, List<String> list) throws LogQueueConnectException;

    boolean setNx(String key, Integer expire);

    boolean existsKey(String key);

    String getMessage(String key);

    String get(String key);

    void set(String key, String value);

    void set(String key, String value, int seconds);

    void expireAt(String key, Long time);

    void expire(String key, int seconds);

    Long incr(String key);

    Long incrBy(String key, int value);

    void hset(String key, Map<String, String> value);

    void sadd(String key, String value);

    Set<String> smembers(String key);

    void del(String key);

    void hset(String key, String field, String value);

    void hdel(String key, String... field);

    String hget(String key, String field);

    Long llen(String key);

    Map<String, String> hgetAll(String key);

    List<String> hmget(String key, String... field);

    Long hincrby(String key, String field, int num);

    List<String> getMessage(String key, int size) throws LogQueueConnectException;

}
