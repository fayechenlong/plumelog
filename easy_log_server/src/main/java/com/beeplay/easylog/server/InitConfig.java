package com.beeplay.easylog.server;


public class InitConfig {
    public static int MAX_SEND_SIZE=100;
    public static String LOG_KEY="beeplay_log_list";
    public static String ES_INDEX="beeplay_log_";
    public static String ES_TYPE="beeplay_log";
    public static String KAFKA_GROUP_NAME="logConsumer";
}
