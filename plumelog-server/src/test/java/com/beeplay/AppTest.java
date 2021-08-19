package com.beeplay;


import com.plumelog.server.client.ElasticLowerClient;
import org.junit.Test;

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


        ElasticLowerClient elasticLowerClient=ElasticLowerClient.getInstance("47.103.115.94:9200","elastic","MoKa1234MOk",false,false);

        System.out.println(elasticLowerClient.getVersion());
    }
}
