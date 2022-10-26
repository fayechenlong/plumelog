package com.plumelog.server.util;

import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.server.InitConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IndexUtil {

    public static String indexName;
    @Value("${plumelog.es.indexName:base}")
    public void setIndexName(String indexName) {
        IndexUtil.indexName = indexName;
    }

    public static String getRunLogIndex(long epochMillis) {
        return indexName + LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_"
                + DateUtil.getDateShortStr(InitConfig.ES_INDEX_ZONE_ID, epochMillis);
    }

    public static String getTraceLogIndex(long epochMillis) {
        return indexName + LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_"
                + DateUtil.getDateShortStr(InitConfig.ES_INDEX_ZONE_ID, epochMillis);
    }

    public static String getRunLogIndexWithHour(long epochMillis) {
        return indexName + LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_RUN + "_"
                + DateUtil.getDateWithHourShortStr(InitConfig.ES_INDEX_ZONE_ID, epochMillis);
    }

    public static String getTraceLogIndexWithHour(long epochMillis) {
        return indexName + LogMessageConstant.ES_INDEX + LogMessageConstant.LOG_TYPE_TRACE + "_"
                + DateUtil.getDateWithHourShortStr(InitConfig.ES_INDEX_ZONE_ID, epochMillis);
    }

}
