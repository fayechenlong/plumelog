package com.plumelog.server.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.RunLogMessage;
import com.plumelog.core.dto.WarningRule;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.server.cache.AppNameCache;
import com.plumelog.server.client.ElasticLowerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.plumelog.server.monitor.DingTalkClient.sendToDingTalk;

/**
 * className：PlumeLogMonitorListener
 * description： 日志监控报警
 * time：2020-07-02.11:19
 *
 * @author Tank
 * @version 1.0.0
 */
@Component
public class PlumeLogMonitorListener implements ApplicationListener<PlumelogMonitorEvent> {

    private static Logger logger = LoggerFactory.getLogger(PlumeLogMonitorListener.class);

    @Autowired
    private PlumeLogMonitorRuleConfig plumeLogMonitorRuleConfig;
    @Autowired
    private RedisClient redisClient;

    private static final String WARNING_NOTICE = ":WARNING:NOTICE";

    @Value("${plumelog.ui.url:127.0.0.1:8989}")
    private String url;

    /**
     * 当KEY设置过期时间时加的后缀
     */
    private static final String KEY_NX = ":NX";



    @Autowired
    private ElasticLowerClient elasticLowerClient;


    @Async
    @Override
    public void onApplicationEvent(PlumelogMonitorEvent event) {
        List<String> logs = event.getLogs();
        List<RunLogMessage> runlogs = new ArrayList<>();
        logs.forEach(logString -> {
            RunLogMessage runLogMessage = JSON.parseObject(logString, RunLogMessage.class);
            //redisClient.sadd(AppNameCache.APP_NAME_SET,runLogMessage.getAppName());
            AppNameCache.appName.add(runLogMessage.getAppName());
            if (runLogMessage.getLogLevel().toUpperCase().equals("ERROR")) {
                runlogs.add(runLogMessage);
            }
        });
        //解析日志
        parserLogMessage(runlogs);
    }

    /**
     * 解析日志
     *
     * @param logMessages 日志
     */
    public void parserLogMessage(List<RunLogMessage> logMessages) {
        logMessages.forEach(runLogMessage -> {
            List<WarningRule> monitorRuleConfig = plumeLogMonitorRuleConfig.getMonitorRuleConfig(runLogMessage.getAppName());
            if (monitorRuleConfig != null) {
                //运行规则
                enforcementRules(monitorRuleConfig, runLogMessage);
            }
        });

    }

    /**
     * 执行日志监控规则
     *
     * @param rules         规则
     * @param runLogMessage 日志
     */
    public void enforcementRules(List<WarningRule> rules, RunLogMessage runLogMessage) {
        for (int i = 0; i < rules.size(); i++) {
            WarningRule warningRule = rules.get(i);
            String className = warningRule.getClassName();
            String appName = warningRule.getAppName();
            if (containsClassName(className, runLogMessage.getClassName())) {
                continue;
            }
            String errorContent = getErrorContent(runLogMessage.getContent());
            //统计分析
            statisticAlnalysis(getKey(appName, className), warningRule, errorContent, runLogMessage.getClassName());
        }
    }

    /**
     * 判断告警路径是否匹配
     *
     * @param cn    告警路径条件
     * @param mcn   日志告警类路径
     * @return
     */
    private boolean containsClassName(String cn, String mcn){
        if (StringUtils.isEmpty(cn)) {
            return false;
        }

        int a = cn.length();
        int b = StringUtils.isEmpty(mcn) ? 0 : mcn.length();

        if (a > b) {
            return true;
        }

        for (int i = 0; i < a; i++) {
            if (cn.charAt(i) != mcn.charAt(i)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 统计分析
     *
     * @param key  缓存key
     * @param rule 规则
     */
    private void statisticAlnalysis(String key, WarningRule rule, String errorContent, String className) {
        String time = redisClient.hget(key, LogMessageConstant.PLUMELOG_MONITOR_KEY_MAP_FILED_TIME);
        if (StringUtils.isEmpty(time)) {
            time=String.valueOf(System.currentTimeMillis());
            redisClient.hset(key, LogMessageConstant.PLUMELOG_MONITOR_KEY_MAP_FILED_TIME, time);
        }
        long startTime = Long.parseLong(time);
        long endTime = startTime + (rule.getTime() * 1000);
        if (endTime > System.currentTimeMillis()) {
            Long incr = redisClient.hincrby(key, LogMessageConstant.PLUMELOG_MONITOR_KEY_MAP_FILED_COUNT, 1);
            if (incr >= rule.getErrorCount() && !redisClient.existsKey(key + WARNING_NOTICE)) {
                earlyWarning(rule, incr, key, errorContent, className);
                redisClient.del(key);
            }
        } else {
            redisClient.hdel(key, LogMessageConstant.PLUMELOG_MONITOR_KEY_MAP_FILED_TIME,
                    LogMessageConstant.PLUMELOG_MONITOR_KEY_MAP_FILED_COUNT);
            redisClient.hincrby(key, LogMessageConstant.PLUMELOG_MONITOR_KEY_MAP_FILED_COUNT, 1);
            redisClient.hset(key, LogMessageConstant.PLUMELOG_MONITOR_KEY_MAP_FILED_TIME,
                    String.valueOf(System.currentTimeMillis()));
        }

    }

    /**
     * 组装key
     *
     * @param appName   应用名
     * @param className 类名
     * @return
     */
    private static String getKey(String appName, String className) {
        String key = LogMessageConstant.PLUMELOG_MONITOR_KEY + appName;
        if (!StringUtils.isEmpty(className)) {
            key = key + ":" + className;
        }
        return key;
    }

    /**
     * 执行预警
     *
     * @param rule
     * @param count
     * @param key
     */
    private void earlyWarning(WarningRule rule, long count, String key, String errorContent, String className) {
        PlumeLogMonitorTextMessage plumeLogMonitorTextMessage =
                new PlumeLogMonitorTextMessage.Builder(rule.getAppName())
                        .className(rule.getClassName())
                        .errorCount(rule.getErrorCount())
                        .time(rule.getTime())
                        .count(count)
                        .monitorUrl(getMonitorMessageURL(rule, className))
                        .errorContent(errorContent)
                        .build();
        if (!StringUtils.isEmpty(rule.getReceiver())) {
            String[] split = rule.getReceiver().split(",");
            List<String> receivers = new ArrayList<String>(Arrays.asList(split));
            if (receivers.contains("all") || receivers.contains("ALL")) {
                plumeLogMonitorTextMessage.setAtAll(true);
               receivers.remove("all");
                receivers.remove("ALL");
            }
            plumeLogMonitorTextMessage.setAtMobiles(receivers);
        }
        String warningKey = key + WARNING_NOTICE;
        if (redisClient.setNx(warningKey + KEY_NX, rule.getTime())) {
            logger.info(plumeLogMonitorTextMessage.getText());
            //default send to dingtalk
            if (rule.getHookServe() == 1) {
                sendToDingTalk(plumeLogMonitorTextMessage, rule.getWebhookUrl());
            } else {
                WechatClient.sendToWeChat(plumeLogMonitorTextMessage, rule.getWebhookUrl());
            }
            sendMesageES(rule, count, errorContent);
        }
        redisClient.set(warningKey, warningKey);
        redisClient.expireAt(warningKey, Long.parseLong(String.valueOf(rule.getTime())));
    }


    /**
     * 报警记录加入至ES
     */
    private void sendMesageES(WarningRule rule, long count, String errorContent) {
        try {
            JSONObject object = (JSONObject) JSONObject.toJSON(rule);
            object.put("count", count);
            object.put("dataTime", System.currentTimeMillis());
            object.put("errorContent", errorContent);
            elasticLowerClient.insertListComm(Arrays.asList(object.toJSONString()),
                    LogMessageConstant.PLUMELOG_MONITOR_MESSAGE_KEY,
                    LogMessageConstant.ES_TYPE);
            logger.info("monitor message insert es success");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("monitor message insert es failed!", e);
        }
    }

    private String getMonitorMessageURL(WarningRule rule, String className) {
        //换算毫秒数
        int time = rule.getTime() * 1000;
        long currentTime = System.currentTimeMillis();
        //开始时间
        long startTime = currentTime - time;
        StringBuilder builder = new StringBuilder(64);
        builder.append(url).append("/#/?appName=").append(rule.getAppName())
                .append("&className=").append(className)
                .append("&logLevel=ERROR")
                .append("&time=").append(startTime).append(",").append(currentTime);
        return builder.toString();
    }

    private String getErrorContent(String content){

        if (content == null) {
            return "";
        }

        int length = 200;

        if (content.length() <= length) {
            return content;
        }

        return content.substring(0, length);
    }
}
