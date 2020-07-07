package com.plumelog.server.monitor;

import com.alibaba.fastjson.JSON;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.WarningRule;
import com.plumelog.core.redis.RedisClient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * className：PlumeLogMonitorRuleConfig
 * description： TODO
 * time：2020-07-02.15:48
 *
 * @author Tank
 * @version 1.0.0
 */
@Component
public class PlumeLogMonitorRuleConfig {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(PlumeLogMonitorListener.class);


    @Autowired
    private RedisClient redisClient;

    private static ConcurrentHashMap<String, List<WarningRule>> configMap = new ConcurrentHashMap<>();


    /**
     * @param appName
     * @return
     */
    public List<WarningRule> getMonitorRuleConfig(String appName) {
        if (configMap.isEmpty()) {
            initMonitorRuleConfig();
        }
        return configMap.get(appName);
    }

    public void initMonitorRuleConfig() {
        Map<String, String> configMap = redisClient.hgetAll(LogMessageConstant.WARN_RULE_KEY);
        Collection<String> values = configMap.values();
        Iterator<String> iterator = values.iterator();
        while (iterator.hasNext()) {
            parserConfig(iterator.next());
        }
    }

    private static void parserConfig(String config) {
        WarningRule warningRule = JSON.parseObject(config, WarningRule.class);
        if (warningRule.getStatus() == 0) {
            return;
        }
        if (configMap.containsKey(warningRule.getAppName())) {
            List<WarningRule> warningRules = configMap.get(warningRule.getAppName());
            warningRules.add(warningRule);
            configMap.put(warningRule.getAppName(), warningRules);
        } else {
            List<WarningRule> lists = new ArrayList<>();
            lists.add(warningRule);
            configMap.put(warningRule.getAppName(), lists);
        }
    }


    @Scheduled(cron = "0 */1 * * * ?")
    private void configureTasks() {
        try {
            initMonitorRuleConfig();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("更新规则配置失败 {}", e);

        }
    }
}
