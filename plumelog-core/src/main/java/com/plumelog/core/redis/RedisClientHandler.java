package com.plumelog.core.redis;

import com.alibaba.fastjson.JSON;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.RedisConfigDTO;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * RedisClientHandler
 *
 * @Author caijian
 * @Date 2020/12/28 6:12 下午
 */
public class RedisClientHandler {

    private ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(1);

    private RedisClientService redisClientService;

    public void init(RedisClientService redisClientService) {
        this.redisClientService = redisClientService;
        scheduled.scheduleWithFixedDelay(() -> pullConfig(), 1, 30, TimeUnit.SECONDS);
    }

    /**
     * 同步config信息
     */
    public void pullConfig() {
        // 获取redis
        try {
            String value = redisClientService.get(LogMessageConstant.CONFIG_REDIS_SET);

            if (value == null || "".equals(value)) {
                return;
            }

            List<RedisConfigDTO> redisConfigs = JSON.parseArray(value, RedisConfigDTO.class);

            if (redisConfigs == null || redisConfigs.size() == 0) {
                return;
            }

            // 注册redis client
            redisConfigs.forEach(r -> {
                RedisClientFactory instance = RedisClientFactory.getInstance();
                instance.registClient(r);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
