package com.plumelog.server.util;

import com.plumelog.core.constant.LogMessageConstant;

import java.util.Date;

public class IndexUtil {

    public static String getRunLogIndex(Date date){
        return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + com.plumelog.server.util.DateUtil.parseDateToStr(date, com.plumelog.server.util.DateUtil.DATE_FORMAT_YYYYMMDD);
    }
    public static String getTraceLogIndex(Date date){
        return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + com.plumelog.server.util.DateUtil.parseDateToStr(date, com.plumelog.server.util.DateUtil.DATE_FORMAT_YYYYMMDD);
    }
    public static String getRunLogIndex(Date date,String hour){
        return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_" + com.plumelog.server.util.DateUtil.parseDateToStr(date, com.plumelog.server.util.DateUtil.DATE_FORMAT_YYYYMMDD)+hour;
    }
    public static String getTraceLogIndex(Date date,String hour){
        return LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_" + com.plumelog.server.util.DateUtil.parseDateToStr(date, com.plumelog.server.util.DateUtil.DATE_FORMAT_YYYYMMDD)+hour;
    }

}
