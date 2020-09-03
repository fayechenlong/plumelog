package com.plumelog.core.redis;


import com.plumelog.core.AbstractClient;
import com.plumelog.core.exception.LogQueueConnectException;
import redis.clients.jedis.*;

import java.util.*;

/**
 * className：RedisClient
 * description：RedisClient instance
 * time：2020-05-11.16:17
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RedisClient extends AbstractClient {
    private static RedisClient instance;
    private int MAX_ACTIVE = 30;
    private int MAX_IDLE = 8;
    private int MAX_WAIT = 1000;
    private int TIMEOUT = 1000;
    private boolean TEST_ON_BORROW = true;
    private JedisPool jedisPool = null;

    private static final String script = "local rs=redis.call(" +
            "'setnx',KEYS[1],ARGV[1]);" +
            "if(rs<1) then return 0;end;" +
            "redis.call('expire',KEYS[1],tonumber(ARGV[2]));" +
            "return 1;";


    public static RedisClient getInstance(String host, int port, String pass,int db) {
        if (instance == null) {
            synchronized (RedisClient.class) {
                if (instance == null) {
                    instance = new RedisClient(host, port, pass,db);
                }
            }
        }
        return instance;
    }

    private RedisClient(String host, int port, String pass) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(MAX_ACTIVE);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT);
        config.setTestOnBorrow(TEST_ON_BORROW);
        if (pass != null && !"".equals(pass)) {
            jedisPool = new JedisPool(config, host, port, TIMEOUT, pass,0);
        } else {
            jedisPool = new JedisPool(config, host, port, TIMEOUT);
        }
    }
    private RedisClient(String host, int port, String pass,int db) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(MAX_ACTIVE);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT);
        config.setTestOnBorrow(TEST_ON_BORROW);
        if (pass != null && !"".equals(pass)) {
            jedisPool = new JedisPool(config, host, port, TIMEOUT, pass,db);
        } else {
            jedisPool = new JedisPool(config, host, port, TIMEOUT);
        }
    }
    @Override
    public void pushMessage(String key, String strings) throws LogQueueConnectException {
        Jedis sj = null;
        try {
            sj = jedisPool.getResource();
            sj.rpush(key, strings);
        } catch (Exception e) {
            throw new LogQueueConnectException("redis 写入失败！", e);
        } finally {
            if (sj != null) {
                sj.close();
            }
        }

    }

    public boolean setNx(String key, Integer expire) {
        if (null == key) {
            return false;
        }
        try {
            Jedis jedis = jedisPool.getResource();
            Long result = (Long) jedis.evalsha(jedis.scriptLoad(script), Arrays.asList(key), Arrays.asList(key, String.valueOf(expire)));
            jedis.close();
            if (result == 1) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public boolean existsKey(String key) {
        Jedis sj = jedisPool.getResource();
        try {
            return sj.exists(key);
        } finally {
            sj.close();
        }

    }

    public String getMessage(String key) {
        Jedis sj = jedisPool.getResource();
        String obj;
        try {
            obj = sj.lpop(key);
        } finally {
            sj.close();
        }
        return obj;
    }

    @Override
    public void putMessageList(String key, List<String> list) throws LogQueueConnectException{
        Jedis sj=null;
        try {
            sj = jedisPool.getResource();
            Pipeline pl = sj.pipelined();
            list.forEach(str -> {
                pl.rpush(key, str);
            });
            pl.sync();
        } catch (Exception e) {
            throw new LogQueueConnectException("redis 写入失败！", e);
        } finally {
            if(sj!=null) {
                sj.close();
            }
        }

    }

    public void set(String key, String value) {
        Jedis sj = jedisPool.getResource();
        try {
            sj.set(key, value);
        } finally {
            sj.close();
        }
    }

    public void set(String key, String value, int seconds) {
        Jedis sj = jedisPool.getResource();
        try {
            Pipeline pl = sj.pipelined();
            pl.set(key, value);
            pl.expire(key, seconds);
            pl.sync();
        } finally {
            sj.close();
        }
    }

    public void expireAt(String key, Long time) {
        Jedis sj = jedisPool.getResource();
        try {
            sj.expireAt(key, time);
        } finally {
            sj.close();
        }
    }

    public void expire(String key, int seconds) {
        Jedis sj = jedisPool.getResource();
        try {
            sj.expire(key, seconds);
        } finally {
            sj.close();
        }
    }

    public Long incr(String key) {
        Long re = 0L;
        Jedis sj = jedisPool.getResource();
        try {
            re = sj.incr(key);
        } finally {
            sj.close();
        }
        return re;
    }

    public Long incrBy(String key, int value) {
        Long re = 0L;
        Jedis sj = jedisPool.getResource();
        try {
            re = sj.incrBy(key, value);
        } finally {
            sj.close();
        }
        return re;
    }

    public void hset(String key, Map<String, String> value) {
        Jedis sj = jedisPool.getResource();
        try {
            sj.hset(key, value);
        } finally {
            sj.close();
        }
    }
    public void sadd(String key, String value) {
        Jedis sj = jedisPool.getResource();
        try {
            sj.sadd(key,value);
        } finally {
            sj.close();
        }
    }
    public Set<String> smembers(String key) {
        Jedis sj = jedisPool.getResource();
        try {
            return sj.smembers(key);
        } finally {
            sj.close();
        }
    }

    public void del(String key) {
        Jedis sj = jedisPool.getResource();
        try {
            sj.del(key);
        } finally {
            sj.close();
        }
    }

    public void hset(String key, String field, String value) {
        Jedis sj = jedisPool.getResource();
        try {
            sj.hset(key, field, value);
        } finally {
            sj.close();
        }
    }

    public void hdel(String key, String... field) {
        Jedis sj = jedisPool.getResource();
        try {
            sj.hdel(key, field);
        } finally {
            sj.close();
        }
    }

    public String hget(String key, String field) {
        String value = "";
        Jedis sj = jedisPool.getResource();
        try {
            value = sj.hget(key, field);
        } finally {
            sj.close();
        }
        return value;
    }
    public Long llen(String key) {
        Long value = 0L;
        Jedis sj = jedisPool.getResource();
        try {
            value= sj.llen(key);
        } finally {
            sj.close();
        }
        return value;
    }
    public Map<String, String> hgetAll(String key) {
        Map<String, String> value = new HashMap<>();
        Jedis sj = jedisPool.getResource();
        try {
            value = sj.hgetAll(key);
        } finally {
            sj.close();
        }
        return value;
    }

    public List<String> hmget(String key, String... field) {
        List<String> value = new ArrayList<>();
        Jedis sj = jedisPool.getResource();
        try {
            value = sj.hmget(key, field);
        } finally {
            sj.close();
        }
        return value;
    }

    public Long hincrby(String key, String field, int num) {
        Long re = 0L;
        Jedis sj = jedisPool.getResource();
        try {
            re = sj.hincrBy(key, field, num);
        } finally {
            sj.close();
        }
        return re;
    }

    public List<String> getMessage(String key, int size) throws LogQueueConnectException {
        Jedis sj = null;
        List<String> list = new ArrayList<>();
        try {
            sj = jedisPool.getResource();
            Long count = sj.llen(key);
            if (count < size) {
                size = count.intValue();
            }
            if (size == 0) {
                return list;
            }
            List<Response<String>> listRes = new ArrayList<>();
            Pipeline pl = sj.pipelined();
            for (int i = 0; i < size; i++) {
                Response<String> res = pl.lpop(key);
                if (res == null) {
                    break;
                }
                listRes.add(res);
            }
            pl.sync();
            listRes.forEach(res -> {
                String log = res.get();
                if (log != null) {
                    list.add(log);
                }
            });
        } catch (Exception e) {
            throw new LogQueueConnectException("redis 连接失败！", e);
        } finally {
            if (sj != null) {
                sj.close();
            }
        }
        return list;
    }
}
