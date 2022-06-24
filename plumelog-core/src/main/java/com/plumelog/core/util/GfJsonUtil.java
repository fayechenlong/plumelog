package com.plumelog.core.util;

//import com.alibaba.fastjson.JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * className：GfJsonUtil
 * description：fastjson工具类
 * time：2020-05-11.16:17
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public abstract class GfJsonUtil {

//    private GfJsonUtil() {
//    }
//
//    public static <T> T parseObject(String json, Class<T> clazz) {
//        if (json == null) {
//            return null;
//        }
//        return JSON.parseObject(json, clazz);
//    }
//
//
//    public static <T> String toJSONString(T t) {
//        if (t == null) {
//            return null;
//        }
//        return JSON.toJSONString(t);
//    }
//
//
//    public static <T> List<T> parseList(Iterable<String> jsonList, Class<T> clazz) {
//        List<T> retList = new ArrayList<T>();
//        for (String json : jsonList) {
//            retList.add(parseObject(json, clazz));
//        }
//        return retList;
//    }
//
//
//    public static <T> List<List<T>> parseArrayList(Iterable<String> jsonList, Class<T> clazz) {
//        List<List<T>> retList = new ArrayList<List<T>>();
//        for (String json : jsonList) {
//            retList.add(parseArray(json, clazz));
//        }
//        return retList;
//    }
//
//    public static <T> List<T> parseArray(String json, Class<T> clazz) {
//        if (json == null) {
//            return Collections.emptyList();
//        }
//        return JSON.parseArray(json, clazz);
//    }
//
//
//    public static <T> Map<Long, T> parseMapByUid(List<String> jsonList, List<Long> uidList, Class<T> clazz, boolean isContainsNull) {
//        if (jsonList.size() != uidList.size()) {
//            return null;
//        }
//        Map<Long, T> uidMap = new HashMap<Long, T>(uidList.size());
//        for (int i = 0; i < uidList.size(); i++) {
//            T t = parseObject(jsonList.get(i), clazz);
//            if (isContainsNull || t != null) {
//                uidMap.put(uidList.get(i), t);
//            }
//        }
//        return uidMap;
//    }
//
//
//    public static <T> Map<Long, List<T>> parseArrayMapByUid(List<String> jsonList, List<Long> uidList, Class<T> clazz, boolean isContainsNull) {
//        if (jsonList.size() != uidList.size()) {
//            return null;
//        }
//        Map<Long, List<T>> uidMap = new HashMap<Long, List<T>>(uidList.size());
//        for (int i = 0; i < uidList.size(); i++) {
//            List<T> t = parseArray(jsonList.get(i), clazz);
//            if (isContainsNull || t != null) {
//                uidMap.put(uidList.get(i), t);
//            }
//        }
//        return uidMap;
//    }


    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T parseObject(String jsonString, Class<T> classz) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return (T) mapper.readValue(jsonString, classz);
        } catch (JsonProcessingException e) {
        }
        return null;
    }

    public static String toJSONString(Object object) {

        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {

        }
        return null;
    }

    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
        try {
            return mapper.readValue(json, javaType);
        } catch (Exception e) {

        }
        return null;
    }

    public static <K, V> Map<K, V> json2Map(String jsonData, Class<K> keyType, Class<V> valueType) {
        JavaType javaType = mapper.getTypeFactory().constructMapType(Map.class, keyType, valueType);
        try {
            return mapper.readValue(jsonData, javaType);
        } catch (Exception e) {
        }
        return null;
    }

}
