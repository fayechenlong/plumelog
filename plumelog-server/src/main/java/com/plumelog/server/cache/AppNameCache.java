package com.plumelog.server.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.plumelog.core.dto.RunLogMessage;

public class AppNameCache {

    private AppNameCache() {
        // Private constructor to prevent instantiation
    }

    private static final String APP_NAME_SET = "plumelog:appname:set";

    private static final Map<String, Set<String>> APP_NAME = new ConcurrentHashMap<>();

    private static AppNameCache INSTANCE = new AppNameCache();

    public static AppNameCache getInstance() {
        return INSTANCE;
    }

    public void addAppName(RunLogMessage runLogMessage) {
        APP_NAME.computeIfAbsent(runLogMessage.getAppName(), k -> ConcurrentHashMap.newKeySet()).add(runLogMessage.getEnv());
    }

    public Map<String, Set<String>> getAppName() {
        return APP_NAME;
    }
}
