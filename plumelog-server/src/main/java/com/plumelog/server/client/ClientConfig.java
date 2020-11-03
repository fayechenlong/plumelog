package com.plumelog.server.client;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.kafka.KafkaConsumerClient;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.server.CollectStartBean;
import com.plumelog.server.InitConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * className：RedisClientConfig
 * description： TODO
 * time：2020-07-02.15:51
 *
 * @author Tank
 * @version 1.0.0
 */
@Configuration
public class ClientConfig implements InitializingBean {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(CollectStartBean.class);
    @Value("${plumelog.model:redis}")
    private String model;
    @Value("${plumelog.kafka.kafkaHosts:}")
    private String kafkaHosts;
    @Value("${plumelog.es.esHosts:}")
    private String esHosts;
    @Value("${plumelog.es.indexType:}")
    private String indexType;
    @Value("${plumelog.es.userName:}")
    private String esUserName;
    @Value("${plumelog.es.passWord:}")
    private String esPassWord;
    @Value("${plumelog.es.shards:5}")
    private int shards;
    @Value("${plumelog.es.replicas:1}")
    private int replicas;
    @Value("${plumelog.es.refresh.interval:60s}")
    private String refreshInterval;
    @Value("${plumelog.es.indexType.model:day}")
    private String indexTypeModel;
    @Value("${plumelog.redis.redisHost:127.0.0.1:6379}")
    private String redisHost;
    @Value("${plumelog.redis.redisPassWord:}")
    private String redisPassWord;
    @Value("${plumelog.redis.redisDb:0}")
    private int redisDb=0;
    @Value("${plumelog.maxSendSize:5000}")
    public int maxSendSize = 5000;
    @Value("${plumelog.interval:100}")
    public int interval = 100;
    @Value("${plumelog.kafka.kafkaGroupName:logConsumer}")
    public String kafkaGroupName = "logConsumer";
    @Value("${plumelog.rest.restUrl:}")
    private String restUrl;
    @Value("${plumelog.rest.restUserName:}")
    private String restUserName;
    @Value("${plumelog.rest.restPassWord:}")
    private String restPassWord;
    @Value("${login.username:}")
    private String loginUsername;
    @Value("${login.password:}")
    private String loginPassword;



    @Bean
    public RedisClient initRedisClient() {
        if (StringUtils.isEmpty(redisHost)) {
            logger.error("can not find redisHost config! please check the application.properties(plumelog.redis.redisHost) ");
            return null;
        }
        String[] hs = redisHost.split(":");
        int port = 6379;
        String ip = "127.0.0.1";
        if (hs.length == 2) {
            ip = hs[0];
            port = Integer.valueOf(hs[1]);
        } else {
            logger.error("redis config error! please check the application.properties(plumelog.redis.redisHost) ");
            return null;
        }
        logger.info("redis host:{},port:{}",ip,port);
        return RedisClient.getInstance(ip, port, redisPassWord,redisDb);
    }

    @Bean
    public ElasticLowerClient initElasticLowerClient() {
        if (StringUtils.isEmpty(esHosts)) {
            logger.error("can not find esHosts config ! please check the application.properties(plumelog.es.esHosts) ");
            return null;
        }
        return ElasticLowerClient.getInstance(esHosts, esUserName, esPassWord);
    }

    @Bean
    public KafkaConsumer initKafkaConsumer() {
        if (InitConfig.KAFKA_MODE_NAME.equals(model)) {
            if (StringUtils.isEmpty(kafkaHosts)) {
                logger.error("can not find kafkaHosts config! please check the application.properties(plumelog.kafka.kafkaHosts) ");
                return null;
            }
            return KafkaConsumerClient.getInstance(kafkaHosts, InitConfig.KAFKA_GROUP_NAME, InitConfig.MAX_SEND_SIZE).getKafkaConsumer();
        } else {
            return null;
        }
    }

    /**
     * 加载配置
     */
    private void loadConfig() {
        InitConfig.MAX_SEND_SIZE = this.maxSendSize;
        InitConfig.KAFKA_GROUP_NAME = this.kafkaGroupName;
        InitConfig.MAX_INTERVAL = this.interval;
        InitConfig.START_MODEL = this.model;

        InitConfig.ES_INDEX_SHARDS=this.shards;
        InitConfig.ES_INDEX_REPLICAS=this.replicas;
        InitConfig.ES_REFRESH_INTERVAL=this.refreshInterval;
        InitConfig.ES_INDEX_MODEL=this.indexTypeModel;

        InitConfig.restUrl = this.restUrl;
        InitConfig.restUserName = this.restUserName;
        InitConfig.restPassWord = this.restPassWord;

        LogMessageConstant.ES_TYPE = this.indexType;

        InitConfig.loginUsername = this.loginUsername;
        InitConfig.loginPassword = this.loginPassword;

        logger.info("server run model:" + this.model);
        logger.info("maxSendSize:" + this.maxSendSize);
        logger.info("interval:" + this.interval);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        try {
            loadConfig();
            logger.info("load config success!");
        } catch (Exception e) {
            logger.error("plumelog load config success failed!", e);
        }
    }
}
