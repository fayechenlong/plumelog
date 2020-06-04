package com.beeplay;

import com.plumelog.ui.es.ElasticLowerClient;

import java.io.IOException;

public class AppTest {

    public static void main( String[] args ) throws IOException {
        ElasticLowerClient elasticLowerClient=ElasticLowerClient.getInstance("172.16.251.196:9200","","");
        elasticLowerClient.deleteIndex("beeplay_log_20200424dasd");

    }
}
