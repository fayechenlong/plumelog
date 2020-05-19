package com.beeplay;

import com.beeplay.easylog.ui.es.ElasticSearchClient;
import org.apache.kafka.common.protocol.types.Field;

import java.util.ArrayList;
import java.util.List;

public class AppTest {

    public static void main( String[] args ){



//        ElasticSearchClient elasticSearchClient=ElasticSearchClient.getInstance("");
//        String[] indexs={"beeplay_log_20200513","easy_log_20200518"};
//        String[] indexsback=elasticSearchClient.getExistIndices(indexs);
//        for(int a=0;a<indexsback.length;a++) {
//            System.out.println(indexsback[a]);
//        }


        List<String> list=new ArrayList<>();
        list.add("1");
        list.add("2");
        List<String> list1=list;

        list1.add("3");

        list.forEach(a->{
            System.out.println("list:"+a);
        });
        list.forEach(a->{
            System.out.println("list1:"+a);
        });

    }
}
