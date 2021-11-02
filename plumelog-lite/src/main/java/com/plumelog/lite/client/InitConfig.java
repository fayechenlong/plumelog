package com.plumelog.lite.client;


public class InitConfig {
    public static String LITE_MODE_LOG_PATH = ".";
    public static final long MILLS_ONE_DAY = 24 * 60 * 60 * 1000;
    //最大每次发送日志条数
    public static int MAX_SEND_SIZE = 5000;
    //日志抓取频次间隔时间
    public static int MAX_INTERVAL = 100;
    //kafka消费组名称
    public static String START_MODEL = "lite";
    public static int ES_INDEX_SHARDS = 5;
    public static int ES_INDEX_REPLICAS = 1;
    public static String ES_REFRESH_INTERVAL = "30s";
    public static String ES_INDEX_MODEL = "day";
    public static String ES_INDEX_ZONE_ID = "GMT+8";
    public static String restUserName = "";
    public static String restPassWord = "";
    public static String restUrl = "";
    public static String loginUsername = "";
    public static String loginPassword = "";
    public static int keepDays = 30;
    public static int traceKeepDays = 0;
}
