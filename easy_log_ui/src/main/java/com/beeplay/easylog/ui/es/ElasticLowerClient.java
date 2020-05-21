package com.beeplay.easylog.ui.es;

import com.beeplay.easylog.core.util.GfJsonUtil;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
* @Author Frank.chen
* @Description //兼容低版本ES的client  不用考虑ES版本问题
* @Date 14:15 2020/5/12
* @Param 
* @return 
**/
public class ElasticLowerClient {
    private  org.slf4j.Logger logger= LoggerFactory.getLogger(ElasticLowerClient.class);
    private static ElasticLowerClient instance;
    private RestClient client;
    public static ElasticLowerClient getInstance(String hosts) {
        if (instance == null) {
            synchronized (ElasticLowerClient.class) {
                if (instance == null) {
                    instance = new ElasticLowerClient(hosts);
                }
            }
        }
        return instance;
    }
    public ElasticLowerClient(String hosts) {
        String[] hostsAndPorts=hosts.split(",");
        HttpHost[] httpHosts = new HttpHost[hostsAndPorts.length];
        for(int i=0;i<hostsAndPorts.length;i++){
            httpHosts[i] = HttpHost.create(hostsAndPorts[i]);
        }
        client = RestClient.builder(httpHosts).build();
    }
    public void close(){
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
