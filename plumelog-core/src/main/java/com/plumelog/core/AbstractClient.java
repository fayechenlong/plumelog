package com.plumelog.core;

import com.plumelog.core.exception.LogQueueConnectException;

import java.util.List;

/**
 * className：AbstractClient
 * description： TODO
 * time：2020-05-13.11:47
 *
 * @author Tank
 * @version 1.0.0
 */
public abstract class AbstractClient {

    private static AbstractClient client;

    public void pushMessage(String key, String strings) throws LogQueueConnectException {}

    public void putMessageList(String key, List<String> list) throws LogQueueConnectException{}

    public static AbstractClient getClient() {
        return client;
    }

    public static void setClient(AbstractClient abstractClient) {
        client = abstractClient;
    }
}
