package com.beeplay.easylog.redis;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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

}
