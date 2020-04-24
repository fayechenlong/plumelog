package com.beeplay.easylog.redis;


import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;

public class RedisClient {
    private static  RedisClient instance;
    private  int MAX_ACTIVE = 8;
    private  int MAX_IDLE = 8;
    private  int MAX_WAIT = 10000;
    private  int TIMEOUT = 10000;
    private  boolean TEST_ON_BORROW = true;
    private JedisPool jedisPool = null;

    public static RedisClient getInstance(String host,int port,String pass) {
        if (instance == null) {
            synchronized (RedisClient.class) {
                if (instance == null) {
                    instance = new RedisClient(host, port, pass);
                }
            }
        }
        return instance;
    }
    private RedisClient(String host, int port, String pass){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(MAX_ACTIVE);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT);
        config.setTestOnBorrow(TEST_ON_BORROW);
        if(pass!=null&&!"".equals(pass)) {
            jedisPool = new JedisPool(config, host, port, TIMEOUT, pass);
        }else{
            jedisPool = new JedisPool(config, host, port, TIMEOUT);
        }
    }
    public void pushMessage(String key, String strings) {
        Jedis sj=jedisPool.getResource();
        try {
            sj.rpush(key, strings);
        }finally {
            sj.close();
        }
    }
    public String getMessage(String key) {
        Jedis sj=jedisPool.getResource();
        String obj;
        try {
            obj=sj.lpop(key);
        }finally {
            sj.close();
        }
        return obj;
    }
    public List<String> getMessage(String key,int size) {
        Jedis sj=jedisPool.getResource();
        List<String> list=new ArrayList<>();
        List<Response<String>> listRes=new ArrayList<>();
        try {
            Pipeline pl=sj.pipelined();
            for(int i=0;i<size;i++) {
                Response<String> res=pl.lpop(key);
                if(res==null){
                    break;
                }
                listRes.add(res);
            }
            pl.sync();
            for(Response<String> res:listRes) {
                String log=res.get();
                if(log!=null) {
                    list.add(log);
                }
            }
        }finally {
            sj.close();
        }
        return list;
    }
}
