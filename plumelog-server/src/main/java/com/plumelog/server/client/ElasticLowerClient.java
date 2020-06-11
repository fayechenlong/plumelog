package com.plumelog.server.client;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * className：ElasticLowerClient
 * description：ElasticLowerClient
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class ElasticLowerClient {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(ElasticLowerClient.class);
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

    public void insertList(List<String> list, String baseIndex, String type) throws IOException {
        StringBuffer sendStr = new StringBuffer();
        for (int a = 0; a < list.size(); a++) {
            String map = list.get(a);
            String ent = "{\"index\":{\"_id\":\"" + UUID.randomUUID().toString() + "\"}} ";
            sendStr.append(ent);
            sendStr.append("\r\n");
            sendStr.append(map);
            sendStr.append("\r\n");
        }
        String endpoint = "";
        if (type == null) {
            endpoint = "/" + baseIndex + "/_bulk";
        } else {
            endpoint = "/" + baseIndex + "/" + type + "/_bulk";
        }
        Request request = new Request(
                "POST",
                endpoint);
        request.setJsonEntity(sendStr.toString());
        client.performRequestAsync(request, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                logger.info("ElasticSearch commit success!");
            }

            @Override
            public void onFailure(Exception e) {
                logger.error("ElasticSearch commit Failure!", e);
            }
        });
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
