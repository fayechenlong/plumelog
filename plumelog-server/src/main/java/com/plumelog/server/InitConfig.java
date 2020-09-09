package com.plumelog.server;


public class InitConfig {
    //最大每次发送日志条数
    public static int MAX_SEND_SIZE = 5000;
    //日志抓取频次间隔时间
    public static int MAX_INTERVAL = 100;
    //kafka消费组名称
    public static String KAFKA_GROUP_NAME = "logConsumer";

    //kafka消费组名称
    public static String START_MODEL = "redis";

    public final static String KAFKA_MODE_NAME = "kafka";
    public final static String REDIS_MODE_NAME = "redis";
    public final static String REST_MODE_NAME = "rest";

    public  static int ES_INDEX_SHARDS = 5;
    public  static int ES_INDEX_REPLICAS = 1;
    public  static String ES_REFRESH_INTERVAL = "30s";
    public  static String ES_INDEX_MODEL = "day";

    public static String restUserName = "";
    public static String restPassWord = "";
    public static String restUrl = "";


    public static String loginUsername = "";
    public static String loginPassword = "";
}
