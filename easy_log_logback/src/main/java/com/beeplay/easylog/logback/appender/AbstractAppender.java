package com.beeplay.easylog.logback.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.beeplay.easylog.core.AbstractClient;
import com.beeplay.easylog.core.constant.LogMessageConstant;
import com.beeplay.easylog.core.dto.BaseLogMessage;
import com.beeplay.easylog.core.dto.RunLogMessage;
import com.beeplay.easylog.core.util.GfJsonUtil;
import com.beeplay.easylog.core.util.ThreadPoolUtil;
import com.beeplay.easylog.logback.util.LogMessageUtil;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * className：AbstractAppender
 * description： TODO
 * time：2020-05-13.11:40
 *
 * @author Tank
 * @version 1.0.0
 */
public abstract class AbstractAppender extends AppenderBase<ILoggingEvent> {
    private ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getPool(4, 8, 5000);

    protected void push(String appName, ILoggingEvent event, AbstractClient client) {
        BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, event);
        final String redisKey =
                logMessage instanceof RunLogMessage
                        ? LogMessageConstant.LOG_KEY
                        : LogMessageConstant.LOG_KEY_TRACE;
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                client.pushMessage(redisKey, GfJsonUtil.toJSONString(logMessage));
            }
        });
    }
}
