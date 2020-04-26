package com.beeplay.easylog.server;

import com.beeplay.easylog.server.collect.KafkaLogCollect;
import com.beeplay.easylog.core.util.ThreadPoolUtil;
import org.apache.log4j.Logger;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;
import static com.beeplay.easylog.server.collect.KafkaLogCollect.kafkaStart;
import static com.beeplay.easylog.server.collect.RedisLogCollect.redisStart;


public class Start {
    private static Logger logger=Logger.getLogger(Start.class);
    private static ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool();
    public static void main( String[] args ) throws Exception {
        Properties properties = new Properties();
        InputStream in = KafkaLogCollect.class.getClassLoader().getResourceAsStream("easylog.properties");
        properties.load(in);

        logger.info("get properties success!");

        String model=properties.getProperty("easylog.server.model");
        InitConfig.MAX_SEND_SIZE=Integer.valueOf(properties.getProperty("easylog.server.maxSendSize"));
        InitConfig.LOG_KEY=properties.getProperty("easylog.server.logKey");
        InitConfig.ES_INDEX=properties.getProperty("easylog.server.es.index");
        InitConfig.ES_TYPE=properties.getProperty("easylog.server.es.type");

        if("kafka".equals(model)) {

            final String kafkaHosts = properties.getProperty("easylog.server.kafka.kafkaHosts");
            final String esHosts = properties.getProperty("easylog.server.es.esHosts");

            logger.info("kafkaHosts:" + kafkaHosts);

            logger.info("esHosts:" + esHosts);

            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    kafkaStart(kafkaHosts, esHosts);
                }
            });
        }
        if("redis".equals(model)) {
            String redisHost = properties.getProperty("easylog.server.redis.redisHost");

            String[] hs=redisHost.split(":");

            final String ip=hs[0];
            final int port=Integer.valueOf(hs[1]);

            final String esHosts = properties.getProperty("easylog.server.es.esHosts");
            logger.info("get properties success!");

            logger.info("redisHost:" + redisHost);

            logger.info("esHosts:" + esHosts);
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    redisStart(ip,port, esHosts);
                }
            });
        }
    }

}
