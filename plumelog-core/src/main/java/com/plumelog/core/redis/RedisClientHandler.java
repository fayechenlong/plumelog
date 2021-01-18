package com.plumelog.core.redis;

import com.alibaba.fastjson.JSON;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.RedisConfigDTO;
import com.plumelog.core.lang.ShutdownHookCallback;
import com.plumelog.core.lang.ShutdownHookCallbacks;

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

        // 添加回调
        ShutdownHookCallbacks.INSTANCE.addCallback(new ShutdownHookCallback() {
            @Override
            public void execute() {
                destroy();
            }
        });
    }

    private void destroy() {
        scheduled.shutdownNow();
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
            RedisClientFactory instance = RedisClientFactory.getInstance();
            instance.regist(redisConfigs);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
