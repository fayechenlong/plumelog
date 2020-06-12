package com.plumelog.server;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.server.collect.KafkaLogCollect;
import com.plumelog.server.collect.RedisLogCollect;
import com.plumelog.server.collect.RestLogCollect;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * className：CollectStartBean
 * description：日誌搜集spring bean
 * time：2020/6/10  17:44
 *
 * @author jdd
 * @version 1.0.0
 */
@Component
@PropertySource(value = "classpath:plumelog.properties", ignoreResourceNotFound = true)
public class CollectStartBean implements InitializingBean {

    @Value("${plumelog.server.model:redis}")
    private String model;
    @Value("${plumelog.server.kafka.kafkaHosts:}")
    private String kafkaHosts;
    @Value("${plumelog.server.es.esHosts:}")
    private String esHosts;
    @Value("${plumelog.server.es.indexType:}")
    private String indexType;
    @Value("${plumelog.server.es.userName:}")
    private String esUserName;
    @Value("${plumelog.server.es.passWord:}")
    private String esPassWord;
    @Value("${plumelog.server.redis.redisHost:127.0.0.1:6379}")
    private String redisHost;
    @Value("${plumelog.server.redis.redisPassWord:}")
    private String redisPassWord;
    @Value("${plumelog.server.maxSendSize:5000}")
    public int maxSendSize = 5000;
    @Value("${plumelog.server.interval:100}")
    public int interval = 100;
    @Value("${plumelog.server.kafka.kafkaGroupName:logConsumer}")
    public String kafkaGroupName = "logConsumer";
    @Value("${plumelog.server.rest.restUrl:}")
    private String restUrl;
    @Value("${plumelog.server.rest.restUserName:}")
    private String restUserName;
    @Value("${plumelog.server.rest.restPassWord:}")
    private String restPassWord;

    private String KAFKA_MODE_NAME = "kafka";
    private String REDIS_MODE_NAME = "redis";
    private String REST_MODE_NAME = "rest";

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(CollectStartBean.class);

    public CollectStartBean() {

    }

    /**
     * 加载配置
     */
    private void loadConfig() {
        InitConfig.MAX_SEND_SIZE = this.maxSendSize;
        InitConfig.KAFKA_GROUP_NAME = this.kafkaGroupName;
        InitConfig.MAX_INTERVAL = this.interval;

        LogMessageConstant.ES_TYPE = this.indexType;

        logger.info("server run model:" + this.model);
        logger.info("maxSendSize:" + this.maxSendSize);
        logger.info("interval:" + this.interval);
    }

    private void serverStart() {
        if (KAFKA_MODE_NAME.equals(model)) {
            if (StringUtils.isEmpty(kafkaHosts)) {
                logger.error("can not find kafkaHosts config! please check the plumelog.properties(plumelog.server.kafka.kafkaHosts) ");
                return;
            }
            KafkaLogCollect kafkaLogCollect = new KafkaLogCollect(this.kafkaHosts, this.esHosts, this.esUserName, this.esPassWord);
            kafkaLogCollect.kafkaStart();
        }
        if (REDIS_MODE_NAME.equals(model)) {
            if (StringUtils.isEmpty(redisHost)) {
                logger.error("can not find redisHost config! please check the plumelog.properties(plumelog.server.redis.redisHost) ");
                return;
            }
            String[] hs = redisHost.split(":");
            int port = 6379;
            String ip = "127.0.0.1";
            if (hs.length == 2) {
                ip = hs[0];
                port = Integer.valueOf(hs[1]);
            } else {
                logger.error("redis config error! please check the plumelog.properties(plumelog.server.redis.redisHost) ");
                return;
            }
            RedisLogCollect redisLogCollect = new RedisLogCollect(ip, port, this.redisPassWord, this.esHosts, this.esUserName, this.esPassWord);
            redisLogCollect.redisStart();
        }
        if (REST_MODE_NAME.equals(model)) {
            if (StringUtils.isEmpty(restUrl)) {
                logger.error("can not find restUrl config! please check the plumelog.properties(plumelog.server.rest.restUrl) ");
                return;
            }
            RestLogCollect restLogCollect = new RestLogCollect(this.restUrl, this.esHosts, this.esUserName, this.esPassWord, this.restUserName, this.restPassWord);
            restLogCollect.restStart();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        try {
            loadConfig();
            logger.info("load config success!");
            serverStart();
        } catch (Exception e) {
            logger.error("plumelog server running fail!", e);
        }
    }
}
