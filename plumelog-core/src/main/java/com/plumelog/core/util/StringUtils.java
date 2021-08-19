package com.plumelog.core.util;

public class StringUtils {

    public static boolean isEmpty(String obj){
        if(obj==null||"".equals(obj)){
            return false;
        }
        return true;
    }
}
