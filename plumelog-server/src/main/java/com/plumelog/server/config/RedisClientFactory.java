package com.plumelog.server.config;

import com.plumelog.core.redis.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Random;

/**
 * RedisClientFactory
 *
 * @Author caijian
 * @Date 2020/12/16 3:59 pm
 */
@Service
public class RedisClientFactory {

    @Autowired
    private List<RedisClient> redisClients;

    @Value("${plumelog.maxSendSize:5000}")
    public int maxSendSize = 5000;
    @Value("${plumelog.maxNoPullTime:5000}")
    public long maxNoPullTime = 5000;

    /**
     * 权重合计值
     */
    private int total = 0;

    @PostConstruct
    public void init() {
        redisClients.forEach(r -> {
            r.setWeight(maxSendSize);
            total += r.getWeight();
        });
    }

    public RedisClient getRedisClient() {
        return randomWeight(total);
    }

    /**
     * 权重随机
     */
    public RedisClient randomWeight(int newTotal) {
        // 权重随机
        Random random = new Random();

        int index = random.nextInt(newTotal);
        // 将 i 设置在 1～total
        index++;
        for (int j = 0; j < redisClients.size(); j++) {
            if (index <= redisClients.get(j).getWeight()) {
                return redisClients.get(j);
            }
            index -= redisClients.get(j).getWeight();
        }

        return null;
    }

    /**
     * 动态权重路由
     */
    public RedisClient pollingSmoothWeight() {

        // 循环判断读取时间超过的时长
        for (RedisClient r : redisClients) {
            if (System.currentTimeMillis() - r.getLatestPullTime() >= maxNoPullTime) {
                return r;
            }
        }
        int total = 0;
        for (RedisClient r : redisClients) {
            total += r.getWeight();
        }

        return randomWeight(total);

    }

}
