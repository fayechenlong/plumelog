package com.beeplay;


import com.beeplay.easylog.server.es.ElasticLowerClient;
import org.junit.Test;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue(){

        ElasticLowerClient elasticLowerClient= ElasticLowerClient.getInstance("172.16.251.196:9200","","");


        String[] aa={"easy_log_20200521","easy_log_20200525a"};

        List<String> bb=elasticLowerClient.getExistIndices(aa);

        bb.forEach(a->{
            System.out.println(a);
        });

    }
}
