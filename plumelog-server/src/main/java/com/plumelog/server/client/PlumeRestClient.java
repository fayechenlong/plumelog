package com.plumelog.server.client;

import com.plumelog.server.controller.Result;
import com.plumelog.server.util.GfJsonUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * className：RestClient
 * description：
 * time：2020/6/10  19:32
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class PlumeRestClient {
    public static List<String> getLogs(String url, String userName, String password) throws IOException {
        List<String> logs=new ArrayList<>();
        Result result=new Result();
        StringEntity stringEntity = new StringEntity("utf-8");
        stringEntity.setContentType("application/json");
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(userName, password));

        CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        HttpPost post = new HttpPost(url);
        post.setEntity(stringEntity);
        post.setConfig(requestConfig);
        HttpResponse response = client.execute(post);
        String re=EntityUtils.toString(response.getEntity());
        if(!StringUtils.isEmpty(re)){
            result= GfJsonUtil.parseObject(re,Result.class);
        }
        if(result!=null){
            if(result.getCode()==200){
                logs=result.getLogs();
            }
        }
        return logs;
    }
}
