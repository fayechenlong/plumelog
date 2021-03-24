package com.plumelog.logback.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.Locale;

/**
 * @author chenlongfei
 */
public class CommFilterSyncLogger extends Filter<ILoggingEvent> {

    private String level;

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getLevel().toString().toLowerCase().equals(level.toLowerCase())) {
            return FilterReply.ACCEPT;
        } else {
            return FilterReply.DENY;
        }
    }
}