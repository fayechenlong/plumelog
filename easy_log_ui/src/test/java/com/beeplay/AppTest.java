package com.beeplay;

import com.beeplay.easylog.ui.es.ElasticSearchClient;
import org.apache.kafka.common.protocol.types.Field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppTest {

    public static void main( String[] args ) throws IOException {



        //ElasticSearchClient elasticSearchClient=ElasticSearchClient.getInstance("172.16.251.196:9200");
        ElasticSearchClient elasticSearchClient=ElasticSearchClient.getInstance("10.33.85.103:9200");
        String[] indexsback=elasticSearchClient.getAllIndices("easy_log_*");
        for(int a=0;a<indexsback.length;a++) {
            System.out.println(indexsback[a]);
        }


    }
}
