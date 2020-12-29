package com.plumelog.core.redis;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.exception.LogQueueConnectException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * RedisClientFactory
 *
 * @Author caijian
 * @Date 2020/12/16 3:59 pm
 */
public class RedisClientFactory extends AbstractClient {

    private volatile static RedisClientFactory instance;
    private static HashMap<String, RedisClientService> map = new HashMap<>();
    public int maxNoPullTime = 30000;
    /**
     * 权重合计值
     */
    private int total = 0;

    private void RedisClientFactory() {
    }

    public static RedisClientFactory getInstance() {
        if (instance == null) {
            synchronized (RedisClientFactory.class) {
                if (instance == null) {
                    instance = new RedisClientFactory();
                    setClient(instance);
                }
            }
        }
        return instance;
    }

    public static RedisClientFactory getInstance(String host, int port, String pass, int db) {
        if (instance == null) {
            synchronized (RedisClientFactory.class) {
                if (instance == null) {
                    registJedisPoolClient(host, port, pass, db);
                    instance = new RedisClientFactory();
                    setClient(instance);
                }
            }
        }
        return instance;
    }

    @Override
    public void pushMessage(String key, String strings) throws LogQueueConnectException {
        // 负载选择
        RedisClientService redisClient = getRedisClient();
        redisClient.pushMessage(key, strings);
    }

    @Override
    public void putMessageList(String key, List<String> list) throws LogQueueConnectException {
        RedisClientService redisClient = getRedisClient();
        redisClient.putMessageList(key, list);
    }

    /**
     * 注册
     *
     * @param host
     * @param port
     * @param pass
     * @param db
     */
    public synchronized static RedisClientService registJedisPoolClient(String host, int port, String pass, int db) {
        // 1。校验是否存在
        String key = host + port + db;
        if (map.containsKey(key)) {
            return map.get(key);
        }
        //todo 创建客户端
        // 2。创建redis客户端
        JedisPoolRedisClient redisClient = new JedisPoolRedisClient(host, port, pass, db);
        map.put(key, redisClient);
        return redisClient;
    }

    /**
     * 获取客户端
     * 走负载策略
     *
     * @return
     */
    public RedisClientService getRedisClient() {
        return randomWeight(total);
    }

    /**
     * 轮询
     */
    public RedisClientService polling() {
        return null;
    }

    /**
     * 权重随机
     */
    public RedisClientService randomWeight(int newTotal) {
        // 权重随机
        Random random = new Random();

        int index = random.nextInt(newTotal);
        // 将 i 设置在 1～total
        index++;
        for (Map.Entry<String, RedisClientService> entry : map.entrySet()) {
            if (index <= entry.getValue().getWeight()) {
                return entry.getValue();
            }
            index -= entry.getValue().getWeight();
        }

        return null;
    }

    /**
     * 动态权重路由
     */
    public RedisClientService pollingSmoothWeight() {

        // 循环判断读取时间超过的时长,防止有些节点一只没有机会
//        for (RedisClientService r : array) {
//            if (System.currentTimeMillis() - r.getLatestPullTime() >= maxNoPullTime) {
//                return r;
//            }
//        }
//        int total = 0;
//        for (RedisClientService r : array) {
//            total += r.getWeight();
//        }

        return randomWeight(total);
    }

}
