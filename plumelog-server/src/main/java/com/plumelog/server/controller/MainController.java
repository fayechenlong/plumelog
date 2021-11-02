package com.plumelog.server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.plumelog.core.client.AbstractClient;
import com.plumelog.core.LogMessage;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.WarningRule;
import com.plumelog.core.dto.WarningRuleDto;
import com.plumelog.core.util.GfJsonUtil;
import com.plumelog.server.InitConfig;
import com.plumelog.server.cache.AppNameCache;
import com.plumelog.core.client.AbstractServerClient;
import com.plumelog.server.controller.vo.LoginVO;
import com.plumelog.server.util.IndexUtil;
import org.elasticsearch.client.ResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * className：MainController
 * description：plumelog server 增強功能：1.開放rest去隊列 1.開發接口插入隊列
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@RestController
@CrossOrigin
public class MainController {

    private final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired(required = false)
    private AbstractClient redisClient;
    @Autowired(required = false)
    private AbstractClient redisQueueClient;

    @Autowired
    private AbstractServerClient abstractServerClient;

    @Value("${admin.password}")
    private String adminPassWord;


    @RequestMapping({"/login", "/plumelogServer/login"})
    public Result login(@RequestBody LoginVO login, HttpServletRequest request) {
        if (StringUtils.isEmpty(InitConfig.loginUsername)) {
            request.getSession().setAttribute("token", new Object());
            return new Result();
        }
        if (InitConfig.loginUsername.equals(login.getUsername()) && InitConfig.loginPassword.equals(login.getPassword())) {
            request.getSession().setAttribute("token", new Object());
            return new Result();
        } else {
            request.getSession().removeAttribute("token");
            return Result.INVALID_LOGIN;
        }
    }

    @RequestMapping({"/logout", "/plumelogServer/logout"})
    public void login(HttpServletRequest request) {
        request.getSession().removeAttribute("token");
    }

    @RequestMapping({"/getlog", "/plumelogServer/getlog"})
    public Result getlog(Integer maxSendSize, String logKey) {
        if (maxSendSize == null) {
            maxSendSize = 500;
        }
        Result result = new Result();
        try {
            List<String> logs = redisClient.getMessage(logKey, maxSendSize);
            if (logs != null && logs.size() > 0) {
                logger.info("get logs success size:" + logs.size());
                result.setCode(200);
                result.setMessage("get logs success!");
                result.setLogs(logs);
                return result;
            }
        } catch (Exception e) {
            logger.error("", e);
            result.setCode(500);
            result.setMessage("get logs error! :" + e.getMessage());
        }
        result.setCode(404);
        result.setMessage("get no logs!");
        return result;
    }

    @RequestMapping({"/sendLog", "/plumelogServer/sendLog"})
    public Result sendLog(@RequestBody List<LogMessage> logs, String logKey) {
        Result result = new Result();
        if ("redis".equals(InitConfig.START_MODEL)) {
            try {
                List<String> logStr = new ArrayList<>();
                logs.forEach(log -> logStr.add(GfJsonUtil.toJSONString(log)));
                redisClient.putMessageList(logKey, logStr);
            } catch (Exception e) {
                result.setCode(500);
                result.setMessage("send logs error! :" + e.getMessage());
            }
            result.setCode(200);
            result.setMessage("send logs! success");
        } else {
            result.setCode(500);
            result.setMessage("send logs error! rest model only support redis model");
        }
        return result;
    }

    @RequestMapping({"/queryAppName", "/plumelog/queryAppName"})
    public String queryAppName(@RequestBody String queryStr) {

        // 查询过去n天的索引
        String[] indexs = new String[InitConfig.keepDays];
        for (int i = 0; i < InitConfig.keepDays; i++) {
            indexs[i] = IndexUtil.getRunLogIndex(
                    System.currentTimeMillis() - i * InitConfig.MILLS_ONE_DAY) + "*";
        }

        // 检查ES索引是否存在
        List<String> reindexs = abstractServerClient.getExistIndices(indexs);
        String indexStr = String.join(",", reindexs);
        if ("".equals(indexStr)) {
            return "";
        }
        logger.info("queryStr:" + queryStr);

        try {
            return abstractServerClient.get(indexStr, queryStr, null, null);
        } catch (Exception e) {
            // 为兼容旧的索引如果按照appNameWithEnv查询失败则重新按照appName查询
            if (e instanceof ResponseException && queryStr.contains("appNameWithEnv")) {
                queryStr = queryStr.replaceAll("appNameWithEnv", "appName");
                logger.info("queryStr:" + queryStr);

                try {
                    return abstractServerClient.get(indexStr, queryStr, null, null);
                } catch (Exception ex) {
                    logger.error("queryAppName fail!", ex);
                    return "";
                }
            }
            logger.error("queryAppName fail!", e);
            return "";
        }
    }

    @RequestMapping({"/queryAppNames", "/plumelog/queryAppNames"})
    public Set<String> queryAppNames(@RequestBody String queryStr) {

        // 查询过去n天的索引
        String[] indexs = new String[InitConfig.keepDays];
        for (int i = 0; i < InitConfig.keepDays; i++) {
            indexs[i] = IndexUtil.getRunLogIndex(
                    System.currentTimeMillis() - i * InitConfig.MILLS_ONE_DAY) + "*";
        }

        // 检查ES索引是否存在
        List<String> reindexs = abstractServerClient.getExistIndices(indexs);
        String indexStr = String.join(",", reindexs);
        if ("".equals(indexStr)) {
            return Collections.emptySet();
        }
        logger.info("queryStr:" + queryStr);
        Set<String> appNameSet = new HashSet<>();
        boolean isQueryWithEnv = queryStr.contains("appNameWithEnv");
        Set<String> appNameWithEnvSet = new TreeSet<>(
                queryAppNameWithEnvSet(indexStr, queryStr, appNameSet, isQueryWithEnv));

        // 为兼容旧的索引及旧的客户端增加按照appName查询的方式
        if (isQueryWithEnv) {
            queryStr = queryStr.replaceAll("appNameWithEnv", "appName");
            logger.info("queryStr:" + queryStr);
            appNameWithEnvSet.addAll(queryAppNameWithEnvSet(indexStr, queryStr, appNameSet, false));
        }
        return appNameWithEnvSet;
    }

    @RequestMapping({"/clientQuery", "/plumelog/clientQuery"})
    public String clientQuery(@RequestBody String queryStr, String size, String from,
                              String clientStartDate, String clientEndDate, String trace) {

        Long clientStartDateTime = 0L;
        try {
            clientStartDateTime = Long.valueOf(clientStartDate);
        } catch (NumberFormatException e) {
            // ignore
        }
        if (clientStartDateTime <= 0) {
            clientStartDateTime = System.currentTimeMillis();
        }

        Long clientEndDateTime = 0L;
        try {
            clientEndDateTime = Long.valueOf(clientEndDate);
        } catch (NumberFormatException e) {
            // ignore
        }

        if (clientEndDateTime <= 0) {
            clientEndDateTime = System.currentTimeMillis();
        }

        Set<String> indexSet = new LinkedHashSet<>();
        while (clientStartDateTime <= clientEndDateTime) {
            indexSet.add(("true".equalsIgnoreCase(trace) ?
                    IndexUtil.getTraceLogIndex(clientStartDateTime) : IndexUtil.getRunLogIndex(clientStartDateTime)) + "*");
            clientStartDateTime += InitConfig.MILLS_ONE_DAY;
        }
        indexSet.add(("true".equalsIgnoreCase(trace) ?
                IndexUtil.getTraceLogIndex(clientEndDateTime) : IndexUtil.getRunLogIndex(clientEndDateTime)) + "*");

        //检查ES索引是否存在
        List<String> existIndices = abstractServerClient.getExistIndices(indexSet.toArray(new String[0]));
        String indexStr = String.join(",", existIndices);
        if ("".equals(indexStr)) {
            return "";
        }
        logger.info("queryStr:" + queryStr);

        try {
            return abstractServerClient.get(indexStr, queryStr, from, size);
        } catch (Exception e) {
            // 为兼容旧的索引如果排序使用seq查询失败则重新按照去掉seq查询
            if (e instanceof ResponseException
                    && (queryStr.contains(",{\"seq\":\"desc\"}") || queryStr.contains(",{\"seq\":\"asc\"}"))) {
                queryStr = queryStr.replace(",{\"seq\":\"desc\"}", "");
                queryStr = queryStr.replace(",{\"seq\":\"asc\"}", "");
                logger.info("queryStr:" + queryStr);

                try {
                    return abstractServerClient.get(indexStr, queryStr, from, size);
                } catch (Exception ex) {
                    logger.error("clientQuery fail!", ex);
                    return "";
                }
            }

            logger.error("clientQuery fail!", e);
            return "";
        }
    }

    @RequestMapping({"/query", "/plumelog/query"})
    public String query(@RequestBody String queryStr, String index, String size, String from, String range) {

        String message = "";
        String indexStr = "";
        try {
            //检查ES索引是否存在
            Set<String> indexSet = new TreeSet<>();
            if (!StringUtils.isEmpty(index)) {
                List<String> indexs = Stream.of(index.split(","))
                        .map(String::trim)
                        .filter(s -> !StringUtils.isEmpty(s))
                        .collect(Collectors.toList());
                if (!indexs.isEmpty()) {
                    indexSet.addAll(indexs);
                }
            }

            if (!StringUtils.isEmpty(range)) {
                int rangeDays = 0;
                if ("day".equalsIgnoreCase(range)) {
                    rangeDays = 1;
                } else if ("week".equalsIgnoreCase(range)) {
                    rangeDays = 7;
                } else if ("month".equalsIgnoreCase(range)) {
                    rangeDays = 30;
                }
                for (int i = 0; i < rangeDays; i++) {
                    indexSet.add(IndexUtil.getRunLogIndex(System.currentTimeMillis() - i * InitConfig.MILLS_ONE_DAY) + "*");
                }
            }
            List<String> reindexs = abstractServerClient.getExistIndices(indexSet.toArray(new String[0]));
            indexStr = reindexs.stream().filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.joining(","));
            if ("".equals(indexStr)) {
                return message;
            }
            logger.info("queryStr:" + queryStr);
            return abstractServerClient.get(indexStr, queryStr, from, size);
        } catch (Exception e) {
            logger.error("query fail!", e);
            return "";
        }
    }

    /**
     * 根据条件删除
     *
     * @param queryStr
     * @param index
     * @param size
     * @param from
     * @return
     */
    @RequestMapping({"/deleteByQuery", "/plumelog/deleteByQuery"})
    public String deleteByQuery(@RequestBody String queryStr, String index, String size, String from) {

        String message = "";
        String indexStr = "";
        try {
            //检查ES索引是否存在
            String[] indexs = Stream.of(index.split(",")).map(String::trim).filter(s -> !StringUtils.isEmpty(s)).toArray(String[]::new);
            List<String> reindexs = abstractServerClient.getExistIndices(indexs);
            indexStr = String.join(",", reindexs);
            if ("".equals(indexStr)) {
                return message;
            }
            String url = "/" + indexStr + "/_delete_by_query?from=" + from + "&size=" + size;
            logger.info("queryURL:" + url);
            logger.info("queryStr:" + queryStr);
            return abstractServerClient.get(url, queryStr);
        } catch (Exception e) {
            logger.error("deleteByQuery fail!", e);
            return "";
        }
    }

    @RequestMapping({"/getServerInfo", "/plumelog/getServerInfo"})
    public String query(String index) {
        return abstractServerClient.cat(index);
    }

    @RequestMapping({"/getQueueCounts", "/plumelog/getQueueCounts"})
    public Map<String, Object> getQueueCounts() {
        Long runSize = 0L;
        Long traceSize = 0L;
        if (redisQueueClient != null) {
            runSize = redisQueueClient.llen(LogMessageConstant.LOG_KEY);
            traceSize = redisQueueClient.llen(LogMessageConstant.LOG_KEY_TRACE);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("runSize", runSize);
        map.put("traceSize", traceSize);
        return map;
    }

    @RequestMapping({"/deleteQueue", "/plumelog/deleteQueue"})
    public Map<String, Object> deleteQueue(String adminPassWord) {
        Map<String, Object> map = new HashMap<>();
        if (adminPassWord.equals(this.adminPassWord)) {
            if (redisQueueClient != null) {
                redisQueueClient.del(LogMessageConstant.LOG_KEY);
                redisQueueClient.del(LogMessageConstant.LOG_KEY_TRACE);
            }
            map.put("acknowledged", true);
        } else {
            map.put("acknowledged", false);
            map.put("message", "管理密码错误！");
        }
        return map;
    }

    @RequestMapping({"/deleteIndex", "/plumelog/deleteIndex"})
    public Map<String, Object> deleteIndex(String index, String adminPassWord) {
        Map<String, Object> map = new HashMap<>();
        if (adminPassWord.equals(this.adminPassWord)) {
            boolean re = abstractServerClient.deleteIndex(index);
            if (index.startsWith(LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN)) {
                creatIndiceLog(index);
            }
            if (index.startsWith(LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE)) {
                creatIndiceTrace(index);
            }
            map.put("acknowledged", re);
        } else {
            map.put("acknowledged", false);
            map.put("message", "管理密码错误！");
        }
        return map;
    }

    private void creatIndiceLog(String index) {
        if (!abstractServerClient.existIndice(index)) {
            abstractServerClient.creatIndice(index, LogMessageConstant.ES_TYPE);
        }
    }

    private void creatIndiceTrace(String index) {
        if (!abstractServerClient.existIndice(index)) {
            abstractServerClient.creatIndiceTrace(index, LogMessageConstant.ES_TYPE);
        }
    }

    @RequestMapping({"/getWarningRuleList", "/plumelog/getWarningRuleList"})
    public Object getWarningRuleList() {
        if (redisClient == null) {
            return null;
        }
        List<WarningRuleDto> list = new ArrayList<>();
        Map<String, String> map = redisClient.hgetAll(LogMessageConstant.WARN_RULE_KEY);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            WarningRule warningRule = GfJsonUtil.parseObject(mapValue, WarningRule.class);
            WarningRuleDto warningRuleDto = new WarningRuleDto();
            warningRuleDto.setId(mapKey);
            warningRuleDto.setAppName(warningRule.getAppName());
            warningRuleDto.setEnv(warningRule.getEnv());
            warningRuleDto.setAppCategory(warningRule.getAppCategory());
            warningRuleDto.setClassName(warningRule.getClassName());
            warningRuleDto.setReceiver(warningRule.getReceiver());
            warningRuleDto.setWebhookUrl(warningRule.getWebhookUrl());
            warningRuleDto.setTime(warningRule.getTime());
            warningRuleDto.setErrorCount(warningRule.getErrorCount());
            warningRuleDto.setStatus(warningRule.getStatus());
            warningRuleDto.setHookServe(warningRule.getHookServe());
            list.add(warningRuleDto);
        }
        return list;
    }

    @RequestMapping({"/saveWarningRuleList", "/plumelog/saveWarningRuleList"})
    public Object saveWarningRule(String id, @RequestBody WarningRule warningRule) {
        if (redisClient == null) {
            return null;
        }
        String warningRuleStr = GfJsonUtil.toJSONString(warningRule);
        redisClient.hset(LogMessageConstant.WARN_RULE_KEY, id, warningRuleStr);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @RequestMapping({"/deleteWarningRule", "/plumelog/deleteWarningRule"})
    public Object deleteWarningRule(String id) {
        if (redisClient == null) {
            return null;
        }
        redisClient.hdel(LogMessageConstant.WARN_RULE_KEY, id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @RequestMapping({"/getExtendfieldList", "/plumelog/getExtendfieldList"})
    public Object getExtendfieldList(String appName) {
        if (redisClient == null) {
            return null;
        }
        return redisClient.hgetAll(LogMessageConstant.EXTEND_APP_MAP_KEY + appName);

    }

    @RequestMapping({"/addExtendfield", "/plumelog/addExtendfield"})
    public Object addExtendfield(String appName, String field, String fieldName) {
        if (redisClient == null) {
            return null;
        }
        redisClient.hset(LogMessageConstant.EXTEND_APP_MAP_KEY + appName, field, fieldName);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @RequestMapping({"/delExtendfield", "/plumelog/delExtendfield"})
    public Object delExtendfield(String appName, String field) {
        if (redisClient == null) {
            return null;
        }
        redisClient.hdel(LogMessageConstant.EXTEND_APP_MAP_KEY + appName, field);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @RequestMapping({"/getAppNames", "/plumelog/getAppNames"})
    public Object getAppNames() {
        return AppNameCache.appName;
    }

    private Set<String> queryAppNameWithEnvSet(String indexStr, String queryStr, Set<String> appNameSet, boolean isQueryWithEnv) {
        try {
            String result = abstractServerClient.group(indexStr, queryStr);
            if (!"".equals(result)) {
                Set<String> appNameWithEnvSet = new HashSet<>();
                JSONObject jsonObject = JSON.parseObject(result);
                jsonObject = (JSONObject) jsonObject.get("aggregations");
                jsonObject = (JSONObject) jsonObject.get("dataCount");
                JSONArray jsonArray = (JSONArray) jsonObject.get("buckets");
                if (isQueryWithEnv) {
                    jsonArray.forEach(key -> {
                        JSONObject keyJsonObject = (JSONObject) key;
                        String appNameWithEnv = (String) keyJsonObject.get("key");
                        appNameWithEnvSet.add(appNameWithEnv);
                        appNameSet.add(appNameWithEnv.split("-_-")[0]);
                    });
                } else {
                    jsonArray.forEach(key -> {
                        JSONObject keyJsonObject = (JSONObject) key;
                        String appName = (String) keyJsonObject.get("key");
                        if (!appNameSet.contains(appName)) {
                            appNameSet.add(appName);
                            appNameWithEnvSet.add(appName + "-_-");
                        }
                    });
                }
                return appNameWithEnvSet;
            }
        } catch (Exception ex) {
            logger.error("queryAppName fail!", ex);
        }
        return Collections.emptySet();
    }

    @RequestMapping({"/getRunModel", "/plumelog/getRunModel"})
    public Object getRunModel() {
        return InitConfig.START_MODEL;
    }
}
