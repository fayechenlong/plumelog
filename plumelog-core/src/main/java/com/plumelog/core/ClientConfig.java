package com.plumelog.core;

/**
 * ClientConfig
 *
 * @Author caijian
 * @Date 2020/12/18 3:26 下午
 */
public class ClientConfig {

    /** 命名空间 */
    public static String NAME_SPANCE;

    /** 客户端应用名称 */
    public static String APP_NAME;

    /** 客户端应用IP */
    public static String SERVER_NAME;

    public static String getNameSpance() {
        return NAME_SPANCE;
    }

    public static void setNameSpance(String nameSpance) {
        NAME_SPANCE = nameSpance;
    }

    public static String getAppName() {
        return APP_NAME;
    }

    public static void setAppName(String appName) {
        APP_NAME = appName;
    }

    public static String getServerName() {
        return SERVER_NAME;
    }

    public static void setServerName(String serverName) {
        SERVER_NAME = serverName;
    }
}
