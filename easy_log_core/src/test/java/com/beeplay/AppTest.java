package com.beeplay;

import static org.junit.Assert.assertTrue;

import com.beeplay.easylog.TransMessage;
import com.beeplay.easylog.core.client.EasyLogger;
import com.beeplay.easylog.core.client.TransId;
import com.beeplay.easylog.kafka.KafkaConsumerClient;
import com.beeplay.easylog.util.IpGetter;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.UUID;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    private static EasyLogger logger = EasyLogger.getLogger(Logger.getLogger(AppTest.class));
    /**
     * Rigorous Test :-)
     */
    @Test
    public void send(){
        TransId.logTranID.set(UUID.randomUUID().toString());
        //for(int i=0;i<1000;i++) {
        logger.info(IpGetter.getIp());
        // }

        try {
            TransMessage a=null;
            a.setTransId("sasa");
        }catch (Exception e){
            logger.error("设置值报错",e);
        }
    }
    @Test
    public void recive(){
        KafkaConsumerClient kcc=new KafkaConsumerClient();
        kcc.runConsumer();
    }
}
