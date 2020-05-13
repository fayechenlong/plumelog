package com.beeplay.easylog.core.constant;

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
     * 链路日志存入ES的索引后缀
     */
    String TRACE_KEY_SUFFIX = "_trace";
    /**
     * 当前链路开始标志
     */
    String TRACE_START = "<";

    /**
     * 当前链路结束标志
     */
    String TRACE_END = ">";


    String LOG_KEY="easy_log_list";

    String ES_INDEX="easy_log_";

    String ES_TYPE="easy_log";

    String LOG_TYPE_RUN="run";

    String LOG_TYPE_TRACE="trace";
}
