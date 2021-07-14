package com.plumelog.server.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AppNameCache {

    public static final String APP_NAME_SET = "plumelog:appname:set";

    public static Map<String, Set<String>> appName = new ConcurrentHashMap<>();
}
