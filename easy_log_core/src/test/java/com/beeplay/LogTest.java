package com.beeplay;

import com.beeplay.easylog.TransMessage;
import com.beeplay.easylog.core.client.EasyLogger;
import com.beeplay.easylog.core.client.TransId;
import com.beeplay.easylog.util.IpGetter;
import org.apache.log4j.Logger;

import java.util.UUID;

/**
 * Hello world!
 *
 */
public class LogTest
{
    private static EasyLogger logger = EasyLogger.getLogger(Logger.getLogger(LogTest.class));
    public static void main( String[] args )
    {
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
}
