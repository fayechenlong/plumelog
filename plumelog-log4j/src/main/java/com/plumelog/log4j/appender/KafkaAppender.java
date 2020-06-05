package com.plumelog.log4j.appender;

import com.plumelog.log4j.util.LogMessageUtil;
import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.kafka.KafkaProducerClient;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * className：KafkaAppender
 * description：KafkaAppender 如果使用kafka作为队列用这个KafkaAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
public class KafkaAppender extends AppenderSkeleton {
    private KafkaProducerClient kafkaClient;
    private String appName;
    private String kafkaHosts;
    private String topic;

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setKafkaHosts(String kafkaHosts) {
        this.kafkaHosts = kafkaHosts;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if (kafkaClient == null) {
            kafkaClient = KafkaProducerClient.getInstance(kafkaHosts);
        }
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(this.appName, loggingEvent);
        MessageAppenderFactory.push(logMessage, kafkaClient);
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
