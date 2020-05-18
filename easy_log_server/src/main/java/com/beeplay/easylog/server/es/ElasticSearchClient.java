package com.beeplay.easylog.server.es;

import com.beeplay.easylog.server.collect.RedisLogCollect;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
/**
* @Author Frank.chen
* @Description //TODO
* @Date 14:15 2020/5/12
* @Param 
* @return 
**/
public class ElasticSearchClient {
    private  org.slf4j.Logger logger= LoggerFactory.getLogger(ElasticSearchClient.class);
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

    public void insertList(List<Map<String,Object>> list,String baseIndex,String type) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        list.forEach(map->{
            IndexRequest request = new IndexRequest(baseIndex);
            request.source(map);
            bulkRequest.add(request);
        });
        client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkResponse) {

                logger.info("ElasticSearch commit success!");
                //成功
            }

            @Override
            public void onFailure(Exception e) {
                //失败
                logger.info("ElasticSearch commit Failure!");
            }
        });
    }
    public void close(){
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
