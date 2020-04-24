package com.beeplay.easylog.server;

import com.beeplay.easylog.server.collect.KafkaLogCollect;
import com.beeplay.easylog.util.ThreadPoolUtil;
import org.apache.log4j.Logger;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;
import static com.beeplay.easylog.server.collect.KafkaLogCollect.kafkaStart;


public class Start {
    private static Logger logger=Logger.getLogger(Start.class);
    private static ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    public static void main( String[] args ) throws Exception {
        Properties properties = new Properties();
        InputStream in = KafkaLogCollect.class.getClassLoader().getResourceAsStream("easylog.properties");
        properties.load(in);

        final String kafkaHosts = properties.getProperty("easylog.server.host.kafkaHosts");
        final String esHosts = properties.getProperty("easylog.server.host.esHosts");
        String model=properties.getProperty("easylog.server.model");
        InitConfig.MAX_SEND_SIZE=Integer.valueOf(properties.getProperty("easylog.server.maxSendSize"));
        logger.info("get properties success!");

        logger.info("kafkaHosts:" + kafkaHosts);

        logger.info("esHosts:" + esHosts);

        if("kafka".equals(model)) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    kafkaStart(kafkaHosts, esHosts);
                }
            });
        }
    }

}
