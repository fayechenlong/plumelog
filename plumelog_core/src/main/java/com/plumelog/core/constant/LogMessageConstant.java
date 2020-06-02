package com.plumelog.core.constant;

/**
 * className：LogMessageConstant
 * description：
 * time：2020-05-12.10:10
 *
 * @author Tank
 * @version 1.0.0
 */
public interface LogMessageConstant {

    /**
     * 链路日志前缀
     */
    String TRACE_PRE = "TRACE:";
    /**
     * 当前链路开始标志
     */
    String TRACE_START = "<";

    /**
     * 当前链路结束标志
     */
    String TRACE_END = ">";


    String LOG_KEY = "plume_log_list";

    /**
     * 链路日志存入ES的索引后缀
     */
    String LOG_KEY_TRACE = "plume_log_list";

    String ES_INDEX = "plume_log_";

    String ES_TYPE = "plume_log";

    String LOG_TYPE_RUN = "run";

    String LOG_TYPE_TRACE = "trace";

    String DELIM_STR = "{}";
}
