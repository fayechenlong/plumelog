package com.plumelog.ui.controller;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.WarningRule;
import com.plumelog.core.dto.WarningRuleDto;
import com.plumelog.core.exception.LogQueueConnectException;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.ui.es.ElasticLowerClient;
import com.plumelog.core.util.GfJsonUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

/**
 * className：MainController
 * description：MainController
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@RestController
@CrossOrigin
public class MainController {


    @Value("${admin.password}")
    private String adminPassWord;
    @Autowired
    private ElasticLowerClient elasticLowerClient;
    @Autowired
    private RedisClient redisClient;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(MainController.class);

    @RequestMapping({"/query", "/plumelog/query"})
    public String query(@RequestBody String queryStr, String index, String size, String from) {

        String message = "";
        String indexStr = "";
        try {
            //检查ES索引是否存在
            String[] indexs = index.split(",");
            List<String> reindexs = elasticLowerClient.getExistIndices(indexs);
            indexStr = String.join(",", reindexs);
            if ("".equals(indexStr)) {
                return message;
            }
            String url = "/" + indexStr + "/_search?from=" + from + "&size=" + size;
            logger.info("queryURL:" + url);
            logger.info("queryStr:" + queryStr);
            return elasticLowerClient.get(url, queryStr);
        } catch (Exception e) {
            logger.error("", e);
            return e.getMessage();
        }
    }

    @RequestMapping({"/getServerInfo", "/plumelog/getServerInfo"})
    public String query(String index) {
        String res = elasticLowerClient.cat(index);
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(res.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        List<String> list = new ArrayList<>();
        try {
            while (true) {
                String aa = br.readLine();
                if (StringUtils.isEmpty(aa)) {
                    break;
                }
                list.add(aa);
            }
            List<Map<String, String>> listMap = new ArrayList<>();
            if (list.size() > 0) {
                String[] title = list.get(0).split("\\s+");
                for (int i = 1; i < list.size(); i++) {
                    String[] values = list.get(i).split("\\s+");
                    Map<String, String> map = new HashMap<>();
                    for (int j = 0; j < title.length; j++) {
                        map.put(title[j], values[j]);
                    }
                    listMap.add(map);
                }
            }
            return GfJsonUtil.toJSONString(listMap);
        } catch (IOException e) {
            logger.error("", e);
        }
        return "";
    }

    @RequestMapping({"/deleteIndex", "/plumelog/deleteIndex"})
    public Map<String, Object> deleteIndex(String index, String adminPassWord) {
        Map<String, Object> map = new HashMap<>();
        if (adminPassWord.equals(this.adminPassWord)) {
            boolean re = elasticLowerClient.deleteIndex(index);
            map.put("acknowledged", re);
        } else {
            map.put("acknowledged", false);
            map.put("message", "管理密码错误！");
        }
        return map;
    }
    @RequestMapping({"/getWarningRuleList", "/plumelog/getWarningRuleList"})
    public Object getWarningRuleList() {
        List<WarningRuleDto> list=new ArrayList<>();
        Map<String,String> map=redisClient.hgetAll(LogMessageConstant.WARN_RULE_KEY);
        for(Map.Entry<String, String> entry : map.entrySet()){
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            WarningRule warningRule=GfJsonUtil.parseObject(mapValue,WarningRule.class);
            WarningRuleDto warningRuleDto=new WarningRuleDto();
            warningRuleDto.setId(mapKey);
            warningRuleDto.setAppName(warningRule.getAppName());
            warningRuleDto.setClassName(warningRule.getClassName());
            warningRuleDto.setReceiver(warningRule.getReceiver());
            warningRuleDto.setWebhookUrl(warningRule.getWebhookUrl());
            warningRuleDto.setTime(warningRule.getTime());
            warningRuleDto.setErrorCount(warningRule.getErrorCount());
            warningRuleDto.setStatus(warningRule.getStatus());
            list.add(warningRuleDto);
        }
        return list;
    }
    @RequestMapping({"/saveWarningRuleList", "/plumelog/saveWarningRuleList"})
    public Object saveWarningRule(String id, @RequestBody WarningRule warningRule) {
        String warningRuleStr=GfJsonUtil.toJSONString(warningRule);
        redisClient.hset(LogMessageConstant.WARN_RULE_KEY,id,warningRuleStr);
        Map<String, Object> result = new HashMap<>();
        result.put("success",true);
        return result;
    }
    @RequestMapping({"/deleteWarningRule", "/plumelog/deleteWarningRule"})
    public Object deleteWarningRule(String id) {
        redisClient.hdel(LogMessageConstant.WARN_RULE_KEY,id);
        Map<String, Object> result = new HashMap<>();
        result.put("success",true);
        return result;
    }



    @RequestMapping({"/getAppNameList", "/plumelog/getAppNameList"})
    public Object getAppNameList() {
        Map<String,String> map=redisClient.hgetAll(LogMessageConstant.EXTEND_APP_KEY);
        return map;
    }
    @RequestMapping({"/addAppName", "/plumelog/addAppName"})
    public Object addAppName(String appName) {
        redisClient.hset(LogMessageConstant.EXTEND_APP_KEY, UUID.randomUUID().toString(),appName);
        Map<String, Object> result = new HashMap<>();
        result.put("success",true);
        return result;
    }
    @RequestMapping({"/delAppName", "/plumelog/delAppName"})
    public Object delAppName(String id) {
        String appName=redisClient.hget(LogMessageConstant.EXTEND_APP_KEY, id);
        redisClient.hdel(LogMessageConstant.EXTEND_APP_KEY, id);
        redisClient.del(LogMessageConstant.EXTEND_APP_MAP_KEY+appName);
        Map<String, Object> result = new HashMap<>();
        result.put("success",true);
        return result;
    }
    @RequestMapping({"/addExtendfield", "/plumelog/addExtendfield"})
    public Object addExtendfield(String appName,String field) {
        redisClient.hset(LogMessageConstant.EXTEND_APP_MAP_KEY+appName, UUID.randomUUID().toString(),field);
        Map<String, Object> result = new HashMap<>();
        result.put("success",true);
        return result;
    }
    @RequestMapping({"/delExtendfield", "/plumelog/delExtendfield"})
    public Object delExtendfield(String appName,String fieldid) {
        redisClient.hdel(LogMessageConstant.EXTEND_APP_MAP_KEY+appName, fieldid);
        Map<String, Object> result = new HashMap<>();
        result.put("success",true);
        return result;
    }
}
