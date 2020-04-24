package com.beeplay.easylog.server.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public void insertOrUpdate(Map map,String baseIndex,String baseType) throws Exception {
        IndexRequest request = new IndexRequest(baseIndex, baseType,  UUID.randomUUID().toString());
        request.source(map);
        client.index(request);
    }
    public void delete(String baseIndex) throws Exception {
        DeleteRequest request = new DeleteRequest(baseIndex);
        client.delete(request);
    }
    public void insertList(List<Map<String,Object>> list,String baseIndex,String baseType) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for(Map<String,Object> map:list){
            IndexRequest request = new IndexRequest(baseIndex, baseType, UUID.randomUUID().toString());
            request.source(map);
            bulkRequest.add(request);
        }
        client.bulk(bulkRequest);
    }
    public void close(){
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
