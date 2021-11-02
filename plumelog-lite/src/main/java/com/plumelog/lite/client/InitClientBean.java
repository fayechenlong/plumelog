package com.plumelog.lite.client;

import com.plumelog.core.client.AbstractServerClient;
import com.plumelog.core.lucene.LuceneClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(1)
public class InitClientBean {
    @Bean
    public AbstractServerClient initAbstractServerClient() {
        return new LuceneClient(InitConfig.LITE_MODE_LOG_PATH);
    }

}
