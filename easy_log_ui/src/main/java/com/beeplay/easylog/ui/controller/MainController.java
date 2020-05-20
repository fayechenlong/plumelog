package com.beeplay.easylog.ui.controller;

import com.beeplay.easylog.ui.es.ElasticSearchClient;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    @RequestMapping("/query")
    public String query(@RequestBody String queryStr,String index,String size,String from) {
        String message="";
        String indexStr=index;

        //加这个try是为了兼容老版本ES
        try {
            ElasticSearchClient elasticSearchClient=ElasticSearchClient.getInstance(esHosts);
            String[] indexs=index.split(",");
            List<String> reindexs=elasticSearchClient.getExistIndices(indexs);
            indexStr=String.join(",",reindexs);
        }catch (Exception e){

        }
        try {
            StringEntity stringEntity = new StringEntity(queryStr, "utf-8");
            stringEntity.setContentType("application/json");
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(5000)
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .build();
            HttpClient client = HttpClientBuilder.create().build();
            String url = "http://"+esHosts+"/"+indexStr+"/_search?from="+from+"&size="+size;
            HttpPost post = new HttpPost(url);
            post.setEntity(stringEntity);
            post.setConfig(requestConfig);
            HttpResponse response = client.execute(post);
            HttpEntity resEntity = response.getEntity();
            message = EntityUtils.toString(resEntity, "utf-8");
        }catch (IOException e){
          e.printStackTrace();
        }
        return message;
    }
}
