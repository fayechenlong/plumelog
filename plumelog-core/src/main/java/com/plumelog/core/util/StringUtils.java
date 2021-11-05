package com.plumelog.core.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtils {

    public static boolean isNotEmpty(String obj){
        if(obj==null||"".equals(obj)){
            return false;
        }
        return true;
    }
    public static boolean isEmpty(String obj){
        if(obj==null||"".equals(obj)){
            return true;
        }
        return false;
    }

    public static Map<String, Object> entityToMap(Object object) {
        Map<String, Object> map = new HashMap<>();
        try {
            Class clazz = object.getClass();
            List fieldsList = new ArrayList<Field[]>();
            while (clazz != null) {
                Field[] declaredFields = clazz.getDeclaredFields();
                fieldsList.add(declaredFields);
                clazz = clazz.getSuperclass();
            }
            for (Object fields:fieldsList) {
                Field[] f = (Field[]) fields;
                for (Field field : f) {
                    field.setAccessible(true);
                    Object o = field.get(object);
                    map.put(field.getName(), o);
                }
            }
        }catch (Exception e){}

        return map;
    }
}
