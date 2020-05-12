package com.beeplay.easylog.server;

import com.beeplay.easylog.server.collect.KafkaLogCollect;
import com.beeplay.easylog.server.collect.RedisLogCollect;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.Properties;
/**
* @Author Frank.chen
* @Description //TODO
* @Date 14:04 2020/5/12
* @Param 
* @return 
**/
public class Start {
    private static org.slf4j.Logger logger= LoggerFactory.getLogger(Start.class);
    private  Properties properties = new Properties();
    private  String model;
    private  String kafkaHosts;
    private  String esHosts;
    private  String redisHost;

    private  String KAFKA_MODE_NAME="kafka";
    private  String REDIS_MODE_NAME="redis";

    /**
    * @Author Frank.chen
    * @Description load config from easylog.properties
    * @Date 14:04 2020/5/12
    * @Param []
    * @return void
    **/
    private  void loadConfig(){
        try {
            InputStream in = KafkaLogCollect.class.getClassLoader().getResourceAsStream("easylog.properties");
            this.properties.load(in);

            InitConfig.MAX_SEND_SIZE=Integer.valueOf(this.properties.getProperty("easylog.server.maxSendSize"));
            InitConfig.LOG_KEY=this.properties.getProperty("easylog.server.logKey");
            InitConfig.ES_INDEX=this.properties.getProperty("easylog.server.es.index");
            InitConfig.ES_TYPE=this.properties.getProperty("easylog.server.es.type");
            InitConfig.KAFKA_GROUP_NAME=this.properties.getProperty("easylog.server.kafka.kafkaGroupName");

            this.kafkaHosts = this.properties.getProperty("easylog.server.kafka.kafkaHosts");
            this.esHosts = this.properties.getProperty("easylog.server.es.esHosts");
            this.redisHost = this.properties.getProperty("easylog.server.redis.redisHost");
            this.model=this.properties.getProperty("easylog.server.model");
        }catch (Exception e){
            logger.error("load config fail!",e);
        }

    }
    /**
    * @Author Frank.chen
    * @Description start server
    * @Date 14:04 2020/5/12
    * @Param []
    * @return void
    **/
    private  void serverStart(){
        loadConfig();
        logger.info("load config success!");
        if(KAFKA_MODE_NAME.equals(model)) {
            logger.info("kafkaHosts:" + kafkaHosts);
            logger.info("esHosts:" + esHosts);
            KafkaLogCollect kafkaLogCollect=new KafkaLogCollect(kafkaHosts, esHosts);
            kafkaLogCollect.kafkaStart();
        }
        if(REDIS_MODE_NAME.equals(model)) {
            String[] hs=redisHost.split(":");
            String ip=hs[0];
            int port=Integer.valueOf(hs[1]);
            logger.info("redisHost:" + redisHost);
            logger.info("esHosts:" + esHosts);
            RedisLogCollect redisLogCollect=new RedisLogCollect(ip,port, esHosts);
            redisLogCollect.redisStart();
        }
    }
    public static void main( String[] args ){
        try {
            Start start=new Start();
            start.serverStart();
        }catch (Exception e){
            logger.error("easyLog server running fail!",e);
        }

    }

}
