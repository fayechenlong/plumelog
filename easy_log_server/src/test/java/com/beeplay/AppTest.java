package com.beeplay;


import com.beeplay.easylog.core.util.IpGetter;
import com.beeplay.easylog.core.util.IpUtils;
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

        try {
            System.out.println(IpUtils.getLocalHostIP());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
