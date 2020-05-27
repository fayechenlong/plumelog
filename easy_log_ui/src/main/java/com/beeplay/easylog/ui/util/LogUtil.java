package com.beeplay.easylog.ui.util;

import com.beeplay.easylog.core.dto.TraceLogMessage;
import com.beeplay.easylog.ui.dto.TraceLog;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName LogUtil
 * @Deacription TODO
 * @Author Frank.Chen
 * @Date 2020/5/18 18:54
 * @Version 1.0
 **/
public class LogUtil {

    public static HttpEntity getInfo(String url,String queryStr,String userName,String password) throws IOException {
        StringEntity stringEntity = new StringEntity(queryStr, "utf-8");
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
        return response.getEntity();
    }

}
