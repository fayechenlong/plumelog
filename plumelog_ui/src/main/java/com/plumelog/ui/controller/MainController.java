package com.plumelog.ui.controller;

import com.plumelog.ui.es.ElasticLowerClient;
import com.plumelog.ui.util.LogUtil;
import com.plumelog.core.util.GfJsonUtil;
import org.apache.http.util.EntityUtils;
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
 * @ClassName MainController
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/18 11:51
 * @Version 1.0
 **/
@RestController
@CrossOrigin
public class MainController {

    @Value("${es.esHosts}")
    private String esHosts;

    @Value("${es.userName}")
    private String userName;

    @Value("${es.passWord}")
    private String passWord;

    @Value("${admin.password}")
    private String adminPassWord;

    @RequestMapping({"/query","/plumelog/query"})
    public String query(@RequestBody String queryStr,String index,String size,String from) {
        String message="";
        String indexStr="";
        try {
            ElasticLowerClient elasticLowerClient=ElasticLowerClient.getInstance(esHosts,userName,passWord);
            String[] indexs=index.split(",");
            List<String> reindexs=elasticLowerClient.getExistIndices(indexs);
            indexStr=String.join(",",reindexs);
            if("".equals(indexStr)){
                return message;
            }
            String url = "http://"+esHosts+"/"+indexStr+"/_search?from="+from+"&size="+size;
            return EntityUtils.toString(LogUtil.getInfo(url,queryStr,userName,passWord), "utf-8");
        }catch (IOException e){
            return e.getMessage();
        }
    }
    @RequestMapping({"/getServerInfo","/plumelog/getServerInfo"})
    public String query(String index) {
        ElasticLowerClient elasticLowerClient=ElasticLowerClient.getInstance(esHosts,userName,passWord);
        String res=elasticLowerClient.cat(index);
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(res.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
        List<String> list=new ArrayList<>();
        try {
            while (true) {
                String aa = br.readLine();
                if(StringUtils.isEmpty(aa)){
                    break;
                }
                list.add(aa);
            }
            List<Map<String,String>> listMap=new ArrayList<>();
            if(list.size()>0){
                String[] title=list.get(0).split("\\s+");
                for(int i=1;i<list.size();i++){
                    String[] values=list.get(i).split("\\s+");
                    Map<String,String> map=new HashMap<>();
                    for(int j=0;j<title.length;j++){
                        map.put(title[j],values[j]);
                    }
                    listMap.add(map);
                }
            }
            return GfJsonUtil.toJSONString(listMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    @RequestMapping({"/deleteIndex","/plumelog/deleteIndex"})
    public Map<String,Object> deleteIndex(String index,String adminPassWord) {
        Map<String, Object> map = new HashMap<>();
        if(adminPassWord.equals(this.adminPassWord)) {
            ElasticLowerClient elasticLowerClient = ElasticLowerClient.getInstance(esHosts, userName, passWord);
            boolean re = elasticLowerClient.deleteIndex(index);
            map.put("acknowledged", re);
        }else {
            map.put("acknowledged", false);
            map.put("message", "管理密码错误！");
        }
        return map;
    }
}
