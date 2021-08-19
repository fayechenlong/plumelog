package com.plumelog.core.redis;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.core.util.StringUtils;
import redis.clients.jedis.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * className：RedisClusterClient
 * description：RedisClusterClient instance
 * time：2020-05-11.16:17
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RedisSentinelClient extends AbstractClient {
    private static final String script = "local rs=redis.call(" +
            "'setnx',KEYS[1],ARGV[1]);" +
            "if(rs<1) then return 0;end;" +
            "redis.call('expire',KEYS[1],tonumber(ARGV[2]));" +
            "return 1;";
    private static RedisSentinelClient instance;
    private JedisSentinelPool jedisSentinelPool = null;
    private final int MAX_ACTIVE = 30;
    private final int MAX_IDLE = 8;
    private final int MAX_WAIT = 1000;
    private final boolean TEST_ON_BORROW = true;

    public RedisSentinelClient(String hosts, String masterName, String pass, int db) {
        String[] clusterHosts = hosts.split(",");
        Set<String> sentinels=new HashSet<>();
        for(int a=0;a<clusterHosts.length;a++){
            sentinels.add(clusterHosts[a]);
        }
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(MAX_ACTIVE);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT);
        config.setTestOnBorrow(TEST_ON_BORROW);
        if (pass != null && !"".equals(pass)) {
            jedisSentinelPool = new JedisSentinelPool(masterName, sentinels, config,Protocol.DEFAULT_TIMEOUT,pass,db);
        } else {
            jedisSentinelPool = new JedisSentinelPool(masterName, sentinels, config,Protocol.DEFAULT_TIMEOUT);
        }
    }

    public static RedisSentinelClient getInstance(String hosts, String masterName, String pass, int db) {
        if (instance == null) {
            synchronized (RedisSentinelClient.class) {
                if (instance == null) {
                    instance = new RedisSentinelClient(hosts, masterName, pass, db);
                }
            }
        }
        return instance;
    }

    @Override
    public void pushMessage(String key, String strings) throws LogQueueConnectException {
        Jedis sj = null;
        try {
            sj = jedisSentinelPool.getResource();
            sj.rpush(key, strings);
        } catch (Exception e) {
            throw new LogQueueConnectException("redis 写入失败！", e);
        } finally {
            if (sj != null) {
                sj.close();
            }
        }

    }

    @Override
    public boolean setNx(String key, Integer expire) {
        if (null == key) {
            return false;
        }
        Jedis jedis = jedisSentinelPool.getResource();
        try {
            Long result = (Long) jedis.evalsha(jedis.scriptLoad(script), Arrays.asList(key), Arrays.asList(key, String.valueOf(expire)));
            jedis.close();
            if (result == 1) {
                return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }

    @Override
    public boolean existsKey(String key) {
        Jedis sj = jedisSentinelPool.getResource();
        try {
            return sj.exists(key);
        } finally {
            sj.close();
        }

    }

    @Override
    public String getMessage(String key) {
        Jedis sj = jedisSentinelPool.getResource();
        String obj;
        try {
            obj = sj.lpop(key);
        } finally {
            sj.close();
        }
        return obj;
    }

    @Override
    public void putMessageList(String key, List<String> list) throws LogQueueConnectException {
        Jedis sj = null;
        try {
            sj = jedisSentinelPool.getResource();
            Pipeline pl = sj.pipelined();
            list.forEach(str -> {
                pl.rpush(key, str);
            });
            pl.sync();
        } catch (Exception e) {
            throw new LogQueueConnectException("redis 写入失败！", e);
        } finally {
            if (sj != null) {
                sj.close();
            }
        }

    }

    @Override
    public void set(String key, String value) {
        Jedis sj = jedisSentinelPool.getResource();
        try {
            sj.set(key, value);
        } finally {
            sj.close();
        }
    }

    @Override
    public void set(String key, String value, int seconds) {
        Jedis sj = jedisSentinelPool.getResource();
        try {
            Pipeline pl = sj.pipelined();
            pl.set(key, value);
            pl.expire(key, seconds);
            pl.sync();
        } finally {
            sj.close();
        }
    }

    @Override
    public void expireAt(String key, Long time) {
        Jedis sj = jedisSentinelPool.getResource();
        try {
            sj.expireAt(key, time);
        } finally {
            sj.close();
        }
    }

    @Override
    public void expire(String key, int seconds) {
        Jedis sj = jedisSentinelPool.getResource();
        try {
            sj.expire(key, seconds);
        } finally {
            sj.close();
        }
    }

    @Override
    public Long incr(String key) {
        Long re = 0L;
        Jedis sj = jedisSentinelPool.getResource();
        try {
            re = sj.incr(key);
        } finally {
            sj.close();
        }
        return re;
    }

    @Override
    public Long incrBy(String key, int value) {
        Long re = 0L;
        Jedis sj = jedisSentinelPool.getResource();
        try {
            re = sj.incrBy(key, value);
        } finally {
            sj.close();
        }
        return re;
    }

    @Override
    public void hset(String key, Map<String, String> value) {
        Jedis sj = jedisSentinelPool.getResource();
        try {
            sj.hset(key, value);
        } finally {
            sj.close();
        }
    }

    @Override
    public void sadd(String key, String value) {
        Jedis sj = jedisSentinelPool.getResource();
        try {
            sj.sadd(key, value);
        } finally {
            sj.close();
        }
    }

    @Override
    public Set<String> smembers(String key) {
        Jedis sj = jedisSentinelPool.getResource();
        try {
            return sj.smembers(key);
        } finally {
            sj.close();
        }
    }

    @Override
    public void del(String key) {
        Jedis sj = jedisSentinelPool.getResource();
        try {
            sj.del(key);
        } finally {
            sj.close();
        }
    }

    @Override
    public void hset(String key, String field, String value) {
        Jedis sj = jedisSentinelPool.getResource();
        try {
            sj.hset(key, field, value);
        } finally {
            sj.close();
        }
    }

    @Override
    public void hdel(String key, String... field) {
        Jedis sj = jedisSentinelPool.getResource();
        try {
            sj.hdel(key, field);
        } finally {
            sj.close();
        }
    }

    @Override
    public String hget(String key, String field) {
        String value = "";
        Jedis sj = jedisSentinelPool.getResource();
        try {
            value = sj.hget(key, field);
        } finally {
            sj.close();
        }
        return value;
    }

    @Override
    public Long llen(String key) {
        Long value = 0L;
        Jedis sj = jedisSentinelPool.getResource();
        try {
            value = sj.llen(key);
        } finally {
            sj.close();
        }
        return value;
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        Map<String, String> value = new HashMap<>();
        Jedis sj = jedisSentinelPool.getResource();
        try {
            value = sj.hgetAll(key);
        } finally {
            sj.close();
        }
        return value;
    }

    @Override
    public List<String> hmget(String key, String... field) {
        List<String> value = new ArrayList<>();
        Jedis sj = jedisSentinelPool.getResource();
        try {
            value = sj.hmget(key, field);
        } finally {
            sj.close();
        }
        return value;
    }

    @Override
    public Long hincrby(String key, String field, int num) {
        Long re = 0L;
        Jedis sj = jedisSentinelPool.getResource();
        try {
            re = sj.hincrBy(key, field, num);
        } finally {
            sj.close();
        }
        return re;
    }

    @Override
    public List<String> getMessage(String key, int size) throws LogQueueConnectException {
        Jedis sj = null;
        List<String> list = new ArrayList<>();
        try {
            sj = jedisSentinelPool.getResource();
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
