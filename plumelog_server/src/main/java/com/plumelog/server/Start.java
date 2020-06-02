package com.plumelog.server;

import com.plumelog.server.collect.KafkaLogCollect;
import com.plumelog.server.collect.RedisLogCollect;
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
    private  String esUserName;
    private  String esPassWord;

    private  String redisHost;
    private  String redisPassWord;

    private  String KAFKA_MODE_NAME="kafka";
    private  String REDIS_MODE_NAME="redis";

    /**
    * @Author Frank.chen
    * @Description load config from plumelog.properties
    * @Date 14:04 2020/5/12
    * @Param []
    * @return void
    **/
    private  void loadConfig(){
        try {
            InputStream in = KafkaLogCollect.class.getClassLoader().getResourceAsStream("plumelog.properties");
            this.properties.load(in);

            InitConfig.MAX_SEND_SIZE=Integer.valueOf(this.properties.getProperty("plumelog.server.maxSendSize"));
            InitConfig.KAFKA_GROUP_NAME=this.properties.getProperty("plumelog.server.kafka.kafkaGroupName");
            InitConfig.MAX_INTERVAL=Integer.valueOf(this.properties.getProperty("plumelog.server.interval"));

            this.kafkaHosts = this.properties.getProperty("plumelog.server.kafka.kafkaHosts");

            this.esHosts = this.properties.getProperty("plumelog.server.es.esHosts");
            this.esUserName=this.properties.getProperty("plumelog.server.es.userName");
            this.esPassWord=this.properties.getProperty("plumelog.server.es.passWord");

            this.redisHost = this.properties.getProperty("plumelog.server.redis.redisHost");
            this.redisPassWord = this.properties.getProperty("plumelog.server.redis.redisPassWord");

            this.model=this.properties.getProperty("plumelog.server.model");
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
            KafkaLogCollect kafkaLogCollect=new KafkaLogCollect(this.kafkaHosts, this.esHosts,this.esUserName,this.esPassWord);
            kafkaLogCollect.kafkaStart();
        }
        if(REDIS_MODE_NAME.equals(model)) {
            String[] hs=redisHost.split(":");
            String ip=hs[0];
            int port=Integer.valueOf(hs[1]);
            logger.info("redisHost:" + redisHost);
            logger.info("esHosts:" + esHosts);
            RedisLogCollect redisLogCollect=new RedisLogCollect(ip,port,this.redisPassWord, this.esHosts,this.esUserName,this.esPassWord);
            redisLogCollect.redisStart();
        }
    }
    public static void main( String[] args ){
        try {
            Start start=new Start();
            start.serverStart();
        }catch (Exception e){
            logger.error("plumelog server running fail!",e);
        }

    }

}
