package com.plumelog.logback.appender;


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.kafka.KafkaProducerClient;
import com.plumelog.logback.util.LogMessageUtil;

/**
 * className：KafkaAppender
 * description：KafkaAppender 如果使用kafka作为队列用这个KafkaAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class KafkaAppender extends AppenderBase<ILoggingEvent> {
    private KafkaProducerClient kafkaClient;
    private String appName;
    private String kafkaHosts;

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setKafkaHosts(String kafkaHosts) {
        this.kafkaHosts = kafkaHosts;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (kafkaClient == null) {
            kafkaClient = KafkaProducerClient.getInstance(this.kafkaHosts);
        }
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(appName, event);
        MessageAppenderFactory.push(logMessage, kafkaClient);
    }
}
