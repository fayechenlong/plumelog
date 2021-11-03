package com.plumelog.lite.client;

import com.plumelog.core.client.AbstractServerClient;
import com.plumelog.core.lucene.LuceneClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@Order(1)
@EnableWebSocket
public class InitClientBean {
    @Bean
    public AbstractServerClient initAbstractServerClient() {
        return new LuceneClient(InitConfig.LITE_MODE_LOG_PATH);
    }
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
