package com.plumelog.server.util;

import com.alibaba.fastjson.JSON;

import java.util.*;

public abstract class GfJsonUtil {

    private GfJsonUtil() {
    }

    /**
     * json转对象
     * 
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        return (T) JSON.parseObject(json, clazz);
    }

    /**
     * 对象转json
     * 
     * @param t
     * @return
     */
    public static <T> String toJSONString(T t) {
        if (t == null) {
            return null;
        }
        return JSON.toJSONString(t);
    }

    /**
     * JSON的字符串Iterable转对象List
     * 
     * @param <T>
     * @param jsonList
     * @param clazz
     * @return
     */
    public static <T> List<T> parseList(Iterable<String> jsonList, Class<T> clazz) {
        List<T> retList = new ArrayList<T>();
        for (String json : jsonList) {
            retList.add(parseObject(json, clazz));
        }
        return retList;
    }

    /**
     * JSON数组的字符串Iterable转对象List
     * 
     * @param <T>
     * @param jsonList
     * @param clazz
     * @return
     */
    public static <T> List<List<T>> parseArrayList(Iterable<String> jsonList, Class<T> clazz) {
        List<List<T>> retList = new ArrayList<List<T>>();
        for (String json : jsonList) {
            retList.add(parseArray(json, clazz));
        }
        return retList;
    }

    /**
     * json转集合
     * 
     * @param json
     * @param clazz
     * @return
     */
    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        if (json == null) {
            return Collections.emptyList();
        }
        return JSON.parseArray(json, clazz);
    }

    /**
     * 对应的UID的对应的JSON字符串转对应集合MAP
     * 
     * @param <T>
     * @param jsonList
     * @param uidList
     * @param clazz
     * @param isContainsNull
     *            MAP中是否包含NULL值
     * @return
     */
    public static <T> Map<Long, T> parseMapByUid(List<String> jsonList, List<Long> uidList, Class<T> clazz, boolean isContainsNull) {
        if (jsonList.size() != uidList.size()) {
            return null;
        }
        Map<Long, T> uidMap = new HashMap<Long, T>(uidList.size());
        for (int i = 0; i < uidList.size(); i++) {
            T t = parseObject(jsonList.get(i), clazz);
            if (isContainsNull || t != null) {
                uidMap.put(uidList.get(i), t);
            }
        }
        return uidMap;
    }

    /**
     * 对应的UID的对应的JSON数组字符串转对应集合MAP
     * 
     * @param <T>
     * @param jsonList
     * @param uidList
     * @param clazz
     * @param isContainsNull
     *            MAP中是否包含NULL值
     * @return
     */
    public static <T> Map<Long, List<T>> parseArrayMapByUid(List<String> jsonList, List<Long> uidList, Class<T> clazz, boolean isContainsNull) {
        if (jsonList.size() != uidList.size()) {
            return null;
        }
        Map<Long, List<T>> uidMap = new HashMap<Long, List<T>>(uidList.size());
        for (int i = 0; i < uidList.size(); i++) {
            List<T> t = parseArray(jsonList.get(i), clazz);
            if (isContainsNull || t != null) {
                uidMap.put(uidList.get(i), t);
            }
        }
        return uidMap;
    }
}
