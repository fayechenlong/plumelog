package com.beeplay.easylog.redis;

import redis.clients.jedis.*;
import java.util.HashSet;
import java.util.Set;

public class RedisClusterClient {
    private static  RedisClusterClient instance;
    private JedisCluster jedisCluster = null;

    public static RedisClusterClient getInstance(String hosts) {
        if (instance == null) {
            synchronized (RedisClient.class) {
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
}
