package com.plumelog.core.redis;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.exception.LogQueueConnectException;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

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
    private static final String script = "local rs=redis.call(" +
            "'setnx',KEYS[1],ARGV[1]);" +
            "if(rs<1) then return 0;end;" +
            "redis.call('expire',KEYS[1],tonumber(ARGV[2]));" +
            "return 1;";
    private static RedisClusterClient instance;
    private JedisCluster jedisCluster = null;
    private final int MAX_ACTIVE = 30;
    private final int MAX_IDLE = 8;
    private final int MAX_WAIT = 1000;
    private final boolean TEST_ON_BORROW = true;

    public RedisClusterClient(String hosts, String pass) {
        String[] clusterHosts = hosts.split(",");
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        for (String hostAndPort : clusterHosts) {
            String[] hap = hostAndPort.split(":");
            jedisClusterNodes.add(new HostAndPort(hap[0].trim(), Integer.parseInt(hap[1].trim())));
        }
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(MAX_ACTIVE);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT);
        config.setTestOnBorrow(TEST_ON_BORROW);
        if (pass != null && !"".equals(pass)) {
            jedisCluster = new JedisCluster(jedisClusterNodes, 200, 200, 1, pass, config);
        } else {
            jedisCluster = new JedisCluster(jedisClusterNodes, 200, 200, 1, config);
        }
    }

    public static RedisClusterClient getInstance(String hosts, String pass) {
        if (instance == null) {
            synchronized (RedisClusterClient.class) {
                if (instance == null) {
                    instance = new RedisClusterClient(hosts, pass);
                }
            }
        }
        return instance;
    }

    @Override
    public List<String> getMessage(String key, int size) {
        List<String> list;
        try {
            list = jedisCluster.lrange(key, 0L, size - 1);
            jedisCluster.ltrim(key, size, -1);
        } finally {
        }
        return list;
    }

    @Override
    public void pushMessage(String key, String strings) throws LogQueueConnectException {
        try {
            jedisCluster.rpush(key, strings);
        } catch (Exception e) {
            throw new LogQueueConnectException("redis 写入失败！", e);
        }
    }

    @Override
    public void putMessageList(String key, List<String> list) throws LogQueueConnectException {
        try {
            list.forEach(str -> {
                jedisCluster.rpush(key, str);
            });
        } catch (Exception e) {
            throw new LogQueueConnectException("redis 写入失败！", e);
        }
    }

    @Override
    public boolean setNx(String key, Integer expire) {
        if (null == key) {
            return false;
        }
        try {
            Long result = (Long) jedisCluster.eval(script, Arrays.asList(key), Arrays.asList(key, String.valueOf(expire)));
            if (result == 1) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean existsKey(String key) {
        try {
            return jedisCluster.exists(key);
        } finally {
        }

    }

    @Override
    public String getMessage(String key) {
        String obj;
        try {
            obj = jedisCluster.lpop(key);
        } finally {
        }
        return obj;
    }

    @Override
    public void set(String key, String value) {
        try {
            jedisCluster.set(key, value);
        } finally {
        }
    }

    @Override
    public void set(String key, String value, int seconds) {
        try {
            jedisCluster.set(key, value);
            jedisCluster.expire(key, seconds);
        } finally {
        }
    }

    @Override
    public void expireAt(String key, Long time) {
        try {
            jedisCluster.expireAt(key, time);
        } finally {
        }
    }

    @Override
    public void expire(String key, int seconds) {
        try {
            jedisCluster.expire(key, seconds);
        } finally {
        }
    }

    @Override
    public Long incr(String key) {
        Long re = 0L;
        try {
            re = jedisCluster.incr(key);
        } finally {
        }
        return re;
    }

    @Override
    public Long incrBy(String key, int value) {
        Long re = 0L;
        try {
            re = jedisCluster.incrBy(key, value);
        } finally {
        }
        return re;
    }

    @Override
    public void hset(String key, Map<String, String> value) {
        try {
            jedisCluster.hset(key, value);
        } finally {
        }
    }

    @Override
    public void sadd(String key, String value) {
        try {
            jedisCluster.sadd(key, value);
        } finally {
        }
    }

    @Override
    public Set<String> smembers(String key) {
        try {
            return jedisCluster.smembers(key);
        } finally {
        }
    }

    @Override
    public void del(String key) {
        try {
            jedisCluster.del(key);
        } finally {
        }
    }

    @Override
    public void hset(String key, String field, String value) {
        try {
            jedisCluster.hset(key, field, value);
        } finally {
        }
    }

    @Override
    public void hdel(String key, String... field) {
        try {
            jedisCluster.hdel(key, field);
        } finally {
        }
    }

    @Override
    public String hget(String key, String field) {
        String value = "";
        try {
            value = jedisCluster.hget(key, field);
        } finally {
        }
        return value;
    }

    @Override
    public Long llen(String key) {
        Long value = 0L;
        try {
            value = jedisCluster.llen(key);
        } finally {
        }
        return value;
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        Map<String, String> value = new HashMap<>();
        try {
            value = jedisCluster.hgetAll(key);
        } finally {
        }
        return value;
    }

    @Override
    public List<String> hmget(String key, String... field) {
        List<String> value = new ArrayList<>();
        try {
            value = jedisCluster.hmget(key, field);
        } finally {
        }
        return value;
    }

    @Override
    public Long hincrby(String key, String field, int num) {
        Long re = 0L;
        try {
            re = jedisCluster.hincrBy(key, field, num);
        } finally {
        }
        return re;
    }
}
