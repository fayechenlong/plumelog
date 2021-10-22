package com.plumelog.server.monitor;

import com.alibaba.fastjson.JSON;
import com.plumelog.core.AbstractClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.WarningRule;
import com.plumelog.server.InitConfig;
import org.slf4j.Logger;
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
    private static final Logger logger = LoggerFactory.getLogger(PlumeLogMonitorListener.class);
    private static ConcurrentHashMap<String, List<WarningRule>> configMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, List<WarningRule>> backConfigMap = new ConcurrentHashMap<>();
    @Autowired(required = false)
    private AbstractClient redisClient;

    private static void parserConfig(String config) {
        WarningRule warningRule = JSON.parseObject(config, WarningRule.class);
        if (warningRule.getStatus() == 0) {
            return;
        }
        String key = getKey(warningRule.getAppName(), warningRule.getEnv());
        if (backConfigMap.containsKey(key)) {
            List<WarningRule> warningRules = backConfigMap.get(key);
            warningRules.add(warningRule);
            backConfigMap.put(key, warningRules);
        } else {
            List<WarningRule> lists = new ArrayList<>();
            lists.add(warningRule);
            backConfigMap.put(key, lists);
        }
    }

    private static String getKey(String appName, String env) {
        return appName + ":" + env;
    }

    /**
     * @param appName
     * @return
     */
    public List<WarningRule> getMonitorRuleConfig(String appName, String env) {
        if (configMap.isEmpty()) {
            initMonitorRuleConfig();
        }
        return configMap.get(getKey(appName, env));
    }

    public synchronized void initMonitorRuleConfig() {
        Map<String, String> configs = redisClient.hgetAll(LogMessageConstant.WARN_RULE_KEY);
        Collection<String> values = configs.values();
        Iterator<String> iterator = values.iterator();
        backConfigMap.clear();
        while (iterator.hasNext()) {
            parserConfig(iterator.next());
        }
        configMap = backConfigMap;
    }

    @Scheduled(cron = "0 */1 * * * ?")
    private void configureTasks() {
        if (!InitConfig.LITE_MODE_NAME.equals(InitConfig.LITE_MODE_NAME)) {
            try {
                initMonitorRuleConfig();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("更新规则配置失败 {}", e);

            }
        }
    }
}
