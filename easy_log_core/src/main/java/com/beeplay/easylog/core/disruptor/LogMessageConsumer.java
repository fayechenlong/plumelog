package com.beeplay.easylog.core.disruptor;

import com.beeplay.easylog.core.constant.LogMessageConstant;
import com.beeplay.easylog.core.dto.BaseLogMessage;
import com.beeplay.easylog.core.dto.RunLogMessage;
import com.beeplay.easylog.core.util.GfJsonUtil;
import com.lmax.disruptor.WorkHandler;

/**
 * className：LogMessageConsumer
 * description： 日志消费
 * time：2020-05-19.13:59
 *
 * @author Tank
 * @version 1.0.0
 */
public class LogMessageConsumer implements WorkHandler<LogEvent> {

    private String name;

    public LogMessageConsumer(String name) {
        this.name = name;
    }

    @Override
    public void onEvent(LogEvent event) throws Exception {
        System.out.println(this.name + "===" + event.getAppName());
        BaseLogMessage baseLogMessage = event.getBaseLogMessage();
        final String redisKey =
                baseLogMessage instanceof RunLogMessage
                        ? LogMessageConstant.LOG_KEY
                        : LogMessageConstant.LOG_KEY_TRACE;
        event.getClient().pushMessage(redisKey, GfJsonUtil.toJSONString(baseLogMessage));
    }
}
