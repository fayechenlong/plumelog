package com.plumelog.logback.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author chenlongfei
 * 自定义过滤器
 */
public class FilterSyncLogger extends Filter<ILoggingEvent> {

    private String level;
    private String filterPackage;

    public void setLevel(String level) {
        this.level = level;
    }

    public void setFilterPackage(String filterPackage) {
        this.filterPackage = filterPackage;
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {

        if (level != null) {
            if (!event.getLevel().toString().equalsIgnoreCase(level)) {
                return FilterReply.DENY;
            } else {
                if (event.getLoggerName().equals(filterPackage)) {
                    return FilterReply.DENY;
                }
            }
        }
        if (event.getLoggerName().equals(filterPackage)) {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

}