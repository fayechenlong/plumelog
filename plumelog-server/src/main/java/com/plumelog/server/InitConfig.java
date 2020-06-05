package com.plumelog.server;


public class InitConfig {
    //最大每次发送日志条数
    public static int MAX_SEND_SIZE=5000;
    //日志抓取频次间隔时间
    public static int MAX_INTERVAL=100;
    //kafka消费组名称
    public static String KAFKA_GROUP_NAME="logConsumer";
}
