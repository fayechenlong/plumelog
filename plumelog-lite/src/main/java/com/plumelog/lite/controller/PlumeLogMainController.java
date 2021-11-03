package com.plumelog.lite.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.plumelog.core.client.AbstractServerClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.lite.client.AppNameCache;
import com.plumelog.lite.client.IndexUtil;
import com.plumelog.lite.client.InitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@CrossOrigin
@RequestMapping("/plumelog")
public class PlumeLogMainController {
    @Autowired
    private AbstractServerClient abstractServerClient;

    private String adminPassWord="123456";

    @RequestMapping({"/queryAppName"})
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
        try {
            return abstractServerClient.get(indexStr, queryStr, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @RequestMapping({"/queryAppNames"})
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
        Set<String> appNameSet = new HashSet<>();
        boolean isQueryWithEnv = queryStr.contains("appNameWithEnv");
        Set<String> appNameWithEnvSet = new TreeSet<>(
                queryAppNameWithEnvSet(indexStr, queryStr, appNameSet, isQueryWithEnv));
        // 为兼容旧的索引及旧的客户端增加按照appName查询的方式
        if (isQueryWithEnv) {
            queryStr = queryStr.replaceAll("appNameWithEnv", "appName");
            appNameWithEnvSet.addAll(queryAppNameWithEnvSet(indexStr, queryStr, appNameSet, false));
        }
        return appNameWithEnvSet;
    }

    @RequestMapping({"/clientQuery"})
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
        try {
            return abstractServerClient.get(indexStr, queryStr, from, size);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @RequestMapping({"/query"})
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
            return abstractServerClient.get(indexStr, queryStr, from, size);
        } catch (Exception e) {
            e.printStackTrace();
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
    @RequestMapping({"/deleteByQuery"})
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
            return abstractServerClient.get(url, queryStr);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @RequestMapping({"/getServerInfo"})
    public String query(String index) {
        return abstractServerClient.cat(index);
    }



    @RequestMapping({"/deleteIndex"})
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



    @RequestMapping({"/getAppNames"})
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
            ex.printStackTrace();
        }
        return Collections.emptySet();
    }

    @GetMapping({"/getConfig"})
    public Object getRunModel() {
        ImmutableMap<String, String> config = ImmutableMap.of("modeName", InitConfig.START_MODEL);
        return ImmutableMap.of(
               "code", 200,
               "data", config
       );
    }
}
