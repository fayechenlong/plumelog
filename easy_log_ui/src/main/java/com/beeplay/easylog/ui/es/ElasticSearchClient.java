package com.beeplay.easylog.ui.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.DeleteAliasRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* @Author Frank.chen
* @Description //TODO
* @Date 14:15 2020/5/12
* @Param 
* @return 
**/
public class ElasticSearchClient {
    private static ElasticSearchClient instance;
    private RestHighLevelClient client;


    public static ElasticSearchClient getInstance(String hosts) {
        if (instance == null) {
            synchronized (ElasticSearchClient.class) {
                if (instance == null) {
                    instance = new ElasticSearchClient(hosts);
                }
            }
        }
        return instance;
    }
    public ElasticSearchClient(String hosts) {
        String[] hostsAndPorts=hosts.split(",");
        HttpHost[] httpHosts = new HttpHost[hostsAndPorts.length];
        for(int i=0;i<hostsAndPorts.length;i++){
            httpHosts[i] = HttpHost.create(hostsAndPorts[i]);
        }
        RestClientBuilder builder = RestClient.builder(httpHosts);
       client = new RestHighLevelClient(builder);
    }
    public List<String> getExistIndices(String [] indices){
        List<String> existIndexList = new ArrayList<String>();
        for (String index: indices){
            try {
                GetIndexRequest indexRequest = new GetIndexRequest(index);
                boolean exists = client.indices().exists(indexRequest, RequestOptions.DEFAULT);
                if (exists) {
                    existIndexList.add(index);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return existIndexList;
    }
    public  String deleteIndex(String indexName) {
        String re="";
        try {
        DeleteRequest deleteRequest=new DeleteRequest(indexName);
        DeleteResponse  deleteResponse=client.delete(deleteRequest, RequestOptions.DEFAULT);
        re= deleteResponse.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return re;
    }
    public String[] getAllIndices(String index) throws IOException {
        GetIndexRequest indexRequest = new GetIndexRequest(index);
        GetIndexResponse getIndexResponse=client.indices().get(indexRequest, RequestOptions.DEFAULT);
        String[] indices=getIndexResponse.getIndices();
        return indices;
    }

    public void close(){
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
