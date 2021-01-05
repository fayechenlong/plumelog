package com.plumelog.core.redis;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.dto.RedisConfigDTO;
import com.plumelog.core.enums.RedisClientEnum;
import com.plumelog.core.exception.LogQueueConnectException;
import redis.clients.jedis.HostAndPort;

import java.util.*;

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

    public static Object llen(String logKey) {
        Long index = 0L;
        for (Map.Entry<String, RedisClientService> entry : map.entrySet()) {
            index += entry.getValue().llen(logKey);
        }
        return index;
    }

    public static void del(String logKey) {
        map.forEach((k, v) -> v.del(logKey));
    }

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

    @Override
    public void pushMessage(String key, String strings) throws LogQueueConnectException {
        // 负载选择
        RedisClientService redisClient = getRedisClient();
        if (redisClient == null) {
            throw new LogQueueConnectException("redisClient not init.");
        }
        redisClient.pushMessage(key, strings);
    }

    @Override
    public void putMessageList(String key, List<String> list) throws LogQueueConnectException {
        RedisClientService redisClient = getRedisClient();
        if (redisClient == null) {
            throw new LogQueueConnectException("redisClient not init.");
        }
        redisClient.putMessageList(key, list);
    }

    /**
     * 注册
     */
    public synchronized void registClient(RedisConfigDTO redisConfig) {
        // 1。校验是否存在
        if (map.containsKey(redisConfig.getConfigId())) {
            return;
        }

        List<RedisConfigDTO.RedisConfigHostAndPort> hostAndPorts = redisConfig.getHostAndPorts();
        if (hostAndPorts == null || hostAndPorts.size() == 0) {
            return;
        }

        RedisClientService redisClientService = null;

        // 创建客户端
        switch (RedisClientEnum.getByValue(redisConfig.getType())) {
            case single:
                redisClientService = new JedisPoolRedisClient(
                        hostAndPorts.get(0).getHost(),
                        hostAndPorts.get(0).getPort(),
                        redisConfig.getPassword(),
                        redisConfig.getIndex());
                break;
            case sentinel:
                Set<String> sentinels = new HashSet<>();

                hostAndPorts.forEach(r -> sentinels.add(r.getHost() + ":" + r.getPort()));

                redisClientService = new JedisSentinelPoolRedisClient(
                        sentinels,
                        redisConfig.getMasterName(),
                        redisConfig.getPassword(),
                        redisConfig.getIndex());
                break;
            case cluster:
                // 添加集群的服务节点Set集合
                Set<HostAndPort> hostAndPortsSet = new HashSet<>();
                // 添加节点
                hostAndPorts.forEach(r -> hostAndPortsSet.add(new HostAndPort(r.getHost(), r.getPort())));

                redisClientService = new JedisClusterPoolRedisClient(hostAndPortsSet, redisConfig.getPassword());
                break;
        }

        map.put(redisConfig.getConfigId(), redisClientService);
        total += redisClientService.getWeight();
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
