package com.plumelog.server.client;

import com.plumelog.core.AbstractClient;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.kafka.KafkaConsumerClient;
import com.plumelog.core.redis.RedisClient;
import com.plumelog.core.redis.RedisClusterClient;
import com.plumelog.core.redis.RedisSentinelClient;
import com.plumelog.server.CollectStartBean;
import com.plumelog.server.InitConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.ZoneId;

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
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CollectStartBean.class);
    @Value("${plumelog.maxSendSize:5000}")
    public int maxSendSize = 5000;
    @Value("${plumelog.interval:100}")
    public int interval = 100;
    @Value("${plumelog.kafka.kafkaGroupName:logConsumer}")
    public String kafkaGroupName = "logConsumer";
    @Value("${plumelog.model:redis}")
    private String model;
    @Value("${plumelog.kafka.kafkaHosts:}")
    private String kafkaHosts;
    /**
     * 支持携带协议，如：http、https
     */
    @Value("${plumelog.es.esHosts:}")
    private String esHosts;
    /**
     * 信任自签证书
     * <p>默认：true
     */
    @Value("${plumelog.es.trustSelfSigned:true}")
    private boolean trustSelfSigned = true;
    /**
     * hostname验证
     * <p>默认：false
     */
    @Value("${plumelog.es.hostnameVerification:false}")
    private boolean hostnameVerification = false;
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
    @Value("${plumelog.es.indexType.zoneId:GMT+8}")
    private String indexTypeZoneId;
    @Value("${plumelog.redis.redisHost:127.0.0.1:6379}")
    private String redisHost;
    @Value("${plumelog.redis.redisPassWord:}")
    private String redisPassWord;
    @Value("${plumelog.redis.redisDb:0}")
    private int redisDb = 0;
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

    @Value("${plumelog.queue.redis.redisHost:''}")
    private String queueRedisHost;
    @Value("${plumelog.queue.redis.sentinel.masterName:''}")
    private String queueRedisSentinelMasterName;
    @Value("${plumelog.queue.redis.redisPassWord:}")
    private String queueRedisPassWord;
    @Value("${plumelog.queue.redis.redisDb:0}")
    private int queueRedisDb = 0;

    @Value("${admin.log.keepDays:0}")
    private int keepDays;
    @Value("${admin.log.trace.keepDays:0}")
    private int traceKeepDays;

    @Bean(name = "redisClient")
    public AbstractClient initRedisClient() {
        if (InitConfig.REDIS_CLUSTER_MODE_NAME.equals(model)) {
            if (StringUtils.isEmpty(queueRedisHost)) {
                logger.error("redis config error! please check the application.properties(plumelog.queue.redis.cluster.nodes) ");
                return null;
            }
            logger.info("manger redis  hosts:{}", queueRedisHost);
            return new RedisClusterClient(queueRedisHost, queueRedisPassWord);
        } else if (InitConfig.REDIS_SENTINEL_MODE_NAME.equals(model)) {
            if (StringUtils.isEmpty(queueRedisHost)) {
                logger.error("redis config error! please check the application.properties(plumelog.queue.redis.sentinel.nodes) ");
                return null;
            }
            logger.info("manger redis hosts:{}", queueRedisHost);
            return new RedisSentinelClient(queueRedisHost, queueRedisSentinelMasterName, queueRedisPassWord, queueRedisDb);
        } else {
            String[] hs = redisHost.split(":");
            int port = 6379;
            String ip = "127.0.0.1";
            if (hs.length == 2) {
                ip = hs[0];
                port = Integer.parseInt(hs[1]);
            } else {
                logger.error("redis config error! please check the application.properties(plumelog.queue.redis.redisHost) ");
                return null;
            }
            logger.info("queue redis host:{},port:{}", ip, port);
            return new RedisClient(ip, port, redisPassWord, redisDb);
        }
    }

    @Bean(name = "redisQueueClient")
    public AbstractClient initRedisQueueClient() {
        if (InitConfig.REDIS_CLUSTER_MODE_NAME.equals(model)) {
            if (StringUtils.isEmpty(queueRedisHost)) {
                logger.error("redis config error! please check the application.properties(plumelog.queue.redis.cluster.nodes) ");
                return null;
            }
            logger.info("queue ClusterRedis hosts:{}", queueRedisHost);
            return new RedisClusterClient(queueRedisHost, queueRedisPassWord);
        }
        if (InitConfig.REDIS_SENTINEL_MODE_NAME.equals(model)) {
            if (StringUtils.isEmpty(queueRedisHost)) {
                logger.error("redis config error! please check the application.properties(plumelog.queue.redis.sentinel.nodes) ");
                return null;
            }
            logger.info("queue redisSentinelNodes hosts:{}", queueRedisHost);
            return new RedisSentinelClient(queueRedisHost, queueRedisSentinelMasterName, queueRedisPassWord, queueRedisDb);
        }
        if (InitConfig.REDIS_MODE_NAME.equals(model)) {
            String[] hs = queueRedisHost.split(":");
            int port = 6379;
            String ip = "127.0.0.1";
            if (hs.length == 2) {
                ip = hs[0];
                port = Integer.parseInt(hs[1]);
            } else {
                logger.error("redis config error! please check the application.properties(plumelog.queue.redis.redisHost) ");
                return null;
            }
            logger.info("queue redis host:{},port:{}", ip, port);
            return new RedisClient(ip, port, queueRedisPassWord, queueRedisDb);
        }
        return null;
    }

    @Bean
    public ElasticLowerClient initElasticLowerClient() {
        if (StringUtils.isEmpty(esHosts)) {
            logger.error("can not find esHosts config ! please check the application.properties(plumelog.es.esHosts) ");
            return null;
        }
        return ElasticLowerClient.getInstance(esHosts, esUserName, esPassWord, trustSelfSigned, hostnameVerification);
    }

    @Bean
    public KafkaConsumer initKafkaConsumer() {
        if (InitConfig.KAFKA_MODE_NAME.equals(model)) {
            if (StringUtils.isEmpty(kafkaHosts)) {
                logger.error("can not find kafkaHosts config! please check the application.properties(plumelog.kafka.kafkaHosts) ");
                return null;
            }
            return KafkaConsumerClient.getInstance(kafkaHosts, InitConfig.KAFKA_GROUP_NAME, InitConfig.MAX_SEND_SIZE).getKafkaConsumer();
        }
        return null;
    }

    /**
     * 加载配置
     */
    private void loadConfig() {
        InitConfig.MAX_SEND_SIZE = this.maxSendSize;
        InitConfig.KAFKA_GROUP_NAME = this.kafkaGroupName;
        InitConfig.MAX_INTERVAL = this.interval;
        InitConfig.START_MODEL = this.model;

        InitConfig.ES_INDEX_SHARDS = this.shards;
        InitConfig.ES_INDEX_REPLICAS = this.replicas;
        InitConfig.ES_REFRESH_INTERVAL = this.refreshInterval;
        InitConfig.ES_INDEX_MODEL = this.indexTypeModel;

        try {
            ZoneId.of(this.indexTypeZoneId);
            InitConfig.ES_INDEX_ZONE_ID = this.indexTypeZoneId;
        } catch (Exception e) {
            logger.error("Please check config 'plumelog.es.indexType.zoneId', the value '{}' is invalid, use default value '{}'!",
                    this.indexTypeZoneId, InitConfig.ES_INDEX_ZONE_ID);
        }

        InitConfig.restUrl = this.restUrl;
        InitConfig.restUserName = this.restUserName;
        InitConfig.restPassWord = this.restPassWord;

        LogMessageConstant.ES_TYPE = this.indexType;

        InitConfig.loginUsername = this.loginUsername;
        InitConfig.loginPassword = this.loginPassword;

        InitConfig.keepDays = this.keepDays;
        InitConfig.traceKeepDays = this.traceKeepDays;

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
