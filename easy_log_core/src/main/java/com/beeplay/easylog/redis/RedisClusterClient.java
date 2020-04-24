package com.beeplay.easylog.redis;

import redis.clients.jedis.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedisClusterClient {
    private static  RedisClusterClient instance;
    private JedisCluster jedisCluster = null;

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
    public void pushMessage(String key, String strings) {
        try {
            jedisCluster.rpush(key, strings);
        }finally {
            jedisCluster.close();
        }
    }
    public String getMessage(String key) {
        String obj;
        try {
            obj=jedisCluster.lpop(key);
        }finally {
            jedisCluster.close();
        }
        return obj;
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
}
