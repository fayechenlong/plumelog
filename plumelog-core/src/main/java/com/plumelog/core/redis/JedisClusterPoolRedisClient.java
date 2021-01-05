package com.plumelog.core.redis;

import com.plumelog.core.exception.LogQueueConnectException;
import redis.clients.jedis.*;
import redis.clients.jedis.util.JedisClusterCRC16;

import java.util.*;

public class JedisClusterPoolRedisClient implements RedisClientService {

    private int MAX_ACTIVE = 30;
    private int MAX_IDLE = 8;
    private int MAX_WAIT = 1000;
    private boolean TEST_ON_BORROW = true;
    private JedisCluster jedisCluster = null;

    // 权重
    private int weight;
    private long latestPullTime;

    private static final String script = "local rs=redis.call(" +
            "'setnx',KEYS[1],ARGV[1]);" +
            "if(rs<1) then return 0;end;" +
            "redis.call('expire',KEYS[1],tonumber(ARGV[2]));" +
            "return 1;";

    public JedisClusterPoolRedisClient(Set<HostAndPort> hostAndPortsSet, String password) {

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(MAX_ACTIVE);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT);
        config.setTestOnBorrow(TEST_ON_BORROW);

        if (password != null && !"".equals(password)) {
            jedisCluster = new JedisCluster(hostAndPortsSet, 2000, 2000, 5, password, config);
        } else {
            jedisCluster = new JedisCluster(hostAndPortsSet, config);
        }
    }

    @Override
    public void shutdown() {
        jedisCluster.close();
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
    public boolean setNx(String key, Integer expire) {
        if (null == key) {
            return false;
        }
        try {
            Long result = (Long) jedisCluster.evalsha(jedisCluster.scriptLoad(script, key), Arrays.asList(key), Arrays.asList(key, String.valueOf(expire)));
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
        return jedisCluster.exists(key);
    }

    @Override
    public String getMessage(String key) {
        String obj = null;
        try {
            obj = jedisCluster.lpop(key);
        } catch (Exception e) {
        }
        return obj;
    }

    @Override
    public String get(String key) {
        String obj = null;
        try {
            obj = jedisCluster.get(key);
        } catch (Exception e) {
        }
        return obj;
    }

    @Override
    public void putMessageList(String key, List<String> list) throws LogQueueConnectException {
        Jedis sj = null;
        try {
            int slot = JedisClusterCRC16.getSlot(key);
            sj = jedisCluster.getConnectionFromSlot(slot);

            Pipeline pl = sj.pipelined();
            list.forEach(str -> pl.rpush(key, str));
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
        try {
            jedisCluster.set(key, value);
        } catch (Exception e) {
        }
    }

    @Override
    public void set(String key, String value, int seconds) {
        try {
            jedisCluster.setex(key, seconds, value);
        } catch (Exception e) {
        }
    }

    @Override
    public void expireAt(String key, Long time) {
        try {
            jedisCluster.expireAt(key, time);
        } catch (Exception e) {
        }
    }

    @Override
    public void expire(String key, int seconds) {
        try {
            jedisCluster.expire(key, seconds);
        } catch (Exception e) {
        }
    }

    @Override
    public Long incr(String key) {
        Long re = 0L;
        try {
            re = jedisCluster.incr(key);
        } catch (Exception e) {
        }
        return re;
    }

    @Override
    public Long incrBy(String key, int value) {
        Long re = 0L;
        try {
            re = jedisCluster.incrBy(key, value);
        } catch (Exception e) {
        }
        return re;
    }

    @Override
    public void hset(String key, Map<String, String> value) {
        try {
            jedisCluster.hset(key, value);
        } catch (Exception e) {
        }
    }

    @Override
    public void sadd(String key, String value) {
        try {
            jedisCluster.sadd(key, value);
        } catch (Exception e) {
        }
    }

    @Override
    public Set<String> smembers(String key) {
        try {
            return jedisCluster.smembers(key);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void del(String key) {
        try {
            jedisCluster.del(key);
        } catch (Exception e) {
        }
    }

    @Override
    public void hset(String key, String field, String value) {
        try {
            jedisCluster.hset(key, field, value);
        } catch (Exception e) {
        }
    }

    @Override
    public void hdel(String key, String... field) {
        try {
            jedisCluster.hdel(key, field);
        } catch (Exception e) {
        }
    }

    @Override
    public String hget(String key, String field) {
        String value = "";
        try {
            value = jedisCluster.hget(key, field);
        } catch (Exception e) {
        }
        return value;
    }

    @Override
    public Long llen(String key) {
        Long value = 0L;
        try {
            value = jedisCluster.llen(key);
        } catch (Exception e) {
        }
        return value;
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        Map<String, String> value = new HashMap<>();
        try {
            value = jedisCluster.hgetAll(key);
        } catch (Exception e) {
        }
        return value;
    }

    @Override
    public List<String> hmget(String key, String... field) {
        List<String> value = new ArrayList<>();
        try {
            value = jedisCluster.hmget(key, field);
        } catch (Exception e) {
        }
        return value;
    }

    @Override
    public Long hincrby(String key, String field, int num) {
        Long re = 0L;
        try {
            re = jedisCluster.hincrBy(key, field, num);
        } catch (Exception e) {
        }
        return re;
    }

    @Override
    public List<String> getMessage(String key, int size) throws LogQueueConnectException {
        Jedis sj = null;
        List<String> list = new ArrayList<>();
        try {
            int slot = JedisClusterCRC16.getSlot(key);
            sj = jedisCluster.getConnectionFromSlot(slot);

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

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public long getLatestPullTime() {
        return latestPullTime;
    }

    @Override
    public void setLatestPullTime(long latestPullTime) {
        this.latestPullTime = latestPullTime;
    }
}
