package com.plumelog.log4j2.appender;

import com.plumelog.core.MessageAppenderFactory;
import com.plumelog.core.constant.LogMessageConstant;
import com.plumelog.core.dto.BaseLogMessage;
import com.plumelog.core.kafka.KafkaProducerClient;
import com.plumelog.log4j2.util.LogMessageUtil;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
/**
 * className：KafkaAppender
 * description：KafkaAppender 如果使用kafka作为队列用这个KafkaAppender输出
 *
 * @author Frank.chen
 * @version 1.0.0
 */
@Plugin(name = "KafkaAppender", category = "Core", elementType = "appender", printObject = true)
public class KafkaAppender extends AbstractAppender {
    private static KafkaProducerClient kafkaClient;
    private String appName;
    private String kafkaHosts;
    private String runModel;

    protected KafkaAppender(String name, String appName, String kafkaHosts,String runModel, Filter filter, Layout<? extends Serializable> layout,
                            final boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        this.appName = appName;
        this.kafkaHosts = kafkaHosts;
        this.runModel=runModel;
    }

    @Override
    public void append(LogEvent logEvent) {
        if (kafkaClient == null) {
            kafkaClient = KafkaProducerClient.getInstance(kafkaHosts);
        }
        final BaseLogMessage logMessage = LogMessageUtil.getLogMessage(this.appName, logEvent);
        MessageAppenderFactory.push(logMessage, kafkaClient,"plume.log.ack");

    }

    @PluginFactory
    public static KafkaAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("appName") String appName,
            @PluginAttribute("kafkaHosts") String kafkaHosts,
            @PluginAttribute("topic") String topic,
            @PluginAttribute("runModel") String runModel,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if(runModel!=null){
            LogMessageConstant.RUN_MODEL=Integer.parseInt(runModel);
        }
        if (kafkaClient == null) {
            kafkaClient = KafkaProducerClient.getInstance(kafkaHosts);
        }
        return new KafkaAppender(name, appName, kafkaHosts,runModel, filter, layout, true);
    }
}
