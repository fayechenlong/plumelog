package com.plumelog.lite.autoconfigure;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@ComponentScan("com.plumelog.lite")
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class PlumelogLiteConfiguration {
}
