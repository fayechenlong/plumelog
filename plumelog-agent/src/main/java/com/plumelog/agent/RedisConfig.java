package com.plumelog.agent;
/**
 * className：RedisConfig
 * description：redis模式配置类
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class RedisConfig {
    public static String appName;
    public static String redisHost;
    public static String redisPort;
    public static String redisAuth;
    public static int redisDb = 0;
    public static String runModel;
    public static String expand;
    public static int maxCount = 100;
    public static int logQueueSize = 10000;
    public static int threadPoolSize = 1;
}
