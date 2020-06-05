package com.plumelog.server.util;

import com.alibaba.fastjson.JSON;

import java.util.*;
/**
 * className：GfJsonUtil
 * description：fastjson封装类
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public abstract class GfJsonUtil {

    private GfJsonUtil() {
    }


    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        return (T) JSON.parseObject(json, clazz);
    }

    public static <T> String toJSONString(T t) {
        if (t == null) {
            return null;
        }
        return JSON.toJSONString(t);
    }

    public static <T> List<T> parseList(Iterable<String> jsonList, Class<T> clazz) {
        List<T> retList = new ArrayList<T>();
        for (String json : jsonList) {
            retList.add(parseObject(json, clazz));
        }
        return retList;
    }

    public static <T> List<List<T>> parseArrayList(Iterable<String> jsonList, Class<T> clazz) {
        List<List<T>> retList = new ArrayList<List<T>>();
        for (String json : jsonList) {
            retList.add(parseArray(json, clazz));
        }
        return retList;
    }
    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        if (json == null) {
            return Collections.emptyList();
        }
        return JSON.parseArray(json, clazz);
    }

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
