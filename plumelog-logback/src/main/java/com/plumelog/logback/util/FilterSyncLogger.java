package com.plumelog.logback.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author chenlongfei
 */
public class FilterSyncLogger extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {

        String filterPackage = "com.plumelog.trace.aspect";

        if (getPackName(event.getLoggerName()).equals(filterPackage)
                || getPackName(event.getLoggerName()).equals(filterPackage)) {
            return FilterReply.DENY;
        } else {
            return FilterReply.ACCEPT;
        }
    }

    public String getPackName(String className) {
        return className.substring(0, className.lastIndexOf("."));
    }

}