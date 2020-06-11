package com.plumelog.ui.es;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * className：ElasticLowerClient
 * description：ElasticLowerClient instance
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class ElasticLowerClient {
    private static ElasticLowerClient instance;
    private RestClient client;

    public static ElasticLowerClient getInstance(String hosts, String userName, String passWord) {
        if (instance == null) {
            synchronized (ElasticLowerClient.class) {
                if (instance == null) {
                    instance = new ElasticLowerClient(hosts, userName, passWord);
                }
            }
        }
        return instance;
    }

    public ElasticLowerClient(String hosts, String userName, String passWord) {

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, passWord));  //es账号密码

        String[] hostsAndPorts = hosts.split(",");
        HttpHost[] httpHosts = new HttpHost[hostsAndPorts.length];
        for (int i = 0; i < hostsAndPorts.length; i++) {
            httpHosts[i] = HttpHost.create(hostsAndPorts[i]);
        }
        client = RestClient.builder(httpHosts).setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                httpClientBuilder.disableAuthCaching();
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }).build();
    }

    public String cat(String index) {
        String reStr = "";
        Request request = new Request(
                "GET",
                "/_cat/indices/" + index + "?v");
        try {
            Response res = client.performRequest(request);

            InputStream inputStream = res.getEntity().getContent();
            byte[] bytes = new byte[0];
            bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            String str = new String(bytes);
            reStr = str;
        } catch (Exception e) {
            e.printStackTrace();
            reStr = "";
        }
        return reStr;
    }
    public String get(String url,String queryStr) {
        String reStr = "";
        StringEntity stringEntity = new StringEntity(queryStr, "utf-8");
        stringEntity.setContentType("application/json");
        Request request = new Request(
                "GET",
                url);
        request.setEntity(stringEntity);
        try {
            Response res = client.performRequest(request);
            return EntityUtils.toString(res.getEntity(),"utf-8");
        } catch (Exception e) {
            reStr = "";
            e.printStackTrace();
        }
        return reStr;
    }

    public List<String> getExistIndices(String[] indices) {
        List<String> existIndexList = new ArrayList<String>();
        for (String index : indices) {
            try {
                Request request = new Request(
                        "HEAD",
                        "/" + index + "");
                Response res = client.performRequest(request);
                if (res.getStatusLine().getStatusCode() == 200) {
                    existIndexList.add(index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return existIndexList;
    }

    public boolean deleteIndex(String index) {
        try {
            Request request = new Request(
                    "DELETE",
                    "/" + index + "");
            Response res = client.performRequest(request);
            if (res.getStatusLine().getStatusCode() == 200) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
