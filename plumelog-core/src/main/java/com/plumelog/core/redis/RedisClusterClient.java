package com.plumelog.core.redis;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.exception.LogQueueConnectException;
import redis.clients.jedis.*;

import java.util.*;

/**
 * className：RedisClusterClient
 * description：RedisClusterClient instance
 * time：2020-05-11.16:17
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RedisClusterClient extends AbstractClient {
    private static  RedisClusterClient instance;
    private JedisCluster jedisCluster = null;
    private static final String script = "local rs=redis.call(" +
            "'setnx',KEYS[1],ARGV[1]);" +
            "if(rs<1) then return 0;end;" +
            "redis.call('expire',KEYS[1],tonumber(ARGV[2]));" +
            "return 1;";
    public static RedisClusterClient getInstance(String hosts) {
        if (instance == null) {
            synchronized (RedisClusterClient.class) {
                if (instance == null) {
                    instance = new RedisClusterClient(hosts);
                }
            }
        }
        return instance;
    }
    private RedisClusterClient(String hosts){
        String [] clusterHosts= hosts.split(",");
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        for(String hostAndPort:clusterHosts){
            String[] hap=hostAndPort.split(":");
            jedisClusterNodes.add(new HostAndPort(hap[0], Integer.parseInt(hap[1])));
        }
        jedisCluster = new JedisCluster(jedisClusterNodes);
    }

    public List<String> getMessage(String key, long size) {
        List<String> list;
        try {
            list=jedisCluster.lrange(key,0L,size-1);
            jedisCluster.ltrim(key,size,-1);
        }finally {
            jedisCluster.close();
        }
        return list;
    }

    @Override
    public void pushMessage(String key, String strings) throws LogQueueConnectException {
        Jedis sj = null;
        try {
            jedisCluster.rpush(key, strings);
        } catch (Exception e) {
            throw new LogQueueConnectException("redis 写入失败！", e);
        } finally {
            if (sj != null) {
                sj.close();
            }
        }

    }

    public boolean existsKey(String key) {
        try {
            return jedisCluster.exists(key);
        } finally {
            jedisCluster.close();
        }

    }

    public String getMessage(String key) {
        String obj;
        try {
            obj = jedisCluster.lpop(key);
        } finally {
            jedisCluster.close();
        }
        return obj;
    }
}
