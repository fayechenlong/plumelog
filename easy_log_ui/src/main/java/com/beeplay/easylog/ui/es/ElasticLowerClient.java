package com.beeplay.easylog.ui.es;

import com.beeplay.easylog.core.util.GfJsonUtil;
import org.apache.http.HttpHost;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    public String cat(String index){
        String reStr="";
        Request request = new Request(
                "GET",
                "/_cat/indices/"+index+"?v");
        try {
            Response res=client.performRequest(request);

            InputStream inputStream=res.getEntity().getContent();
            byte[] bytes = new byte[0];
            bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            String str = new String(bytes);
            reStr=str;
        } catch (IOException e) {
            reStr = "";
        }
       return reStr;
    }
    public List<String> getExistIndices(String [] indices){
        List<String> existIndexList = new ArrayList<String>();
        for (String index: indices){
            try {
                Request request = new Request(
                        "PUT",
                        "/"+index+"");
                Response res=client.performRequest(request);
                if(res.getStatusLine().getStatusCode()==200){
                    existIndexList.add(index);
                }
            } catch (IOException e) {
            }
        }
        return existIndexList;
    }
    public void close(){
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
