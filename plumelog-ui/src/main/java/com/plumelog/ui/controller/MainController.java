package com.plumelog.ui.controller;

import com.plumelog.ui.es.ElasticLowerClient;
import com.plumelog.core.util.GfJsonUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * className：MainController
 * description：MainController
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@RestController
@CrossOrigin
public class MainController implements InitializingBean {

    @Value("${es.esHosts}")
    private String esHosts;

    @Value("${es.userName:}")
    private String userName;

    @Value("${es.passWord:}")
    private String passWord;

    @Value("${admin.password}")
    private String adminPassWord;

    private ElasticLowerClient elasticLowerClient;

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

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.elasticLowerClient == null) {
            logger.info("esHosts:" + esHosts);
            logger.info("es.userName:" + userName);
            logger.info("es.passWord:" + passWord);
            this.elasticLowerClient = ElasticLowerClient.getInstance(esHosts, userName, passWord);
            logger.info("Initializing elasticLowerClient success! esHosts:{}", esHosts);
        }
    }
}
