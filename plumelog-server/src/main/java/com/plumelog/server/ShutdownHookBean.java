package com.plumelog.server;

import com.plumelog.core.lang.PlumeShutdownHook;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * ShutdownHookBean
 *
 * @Author caijian
 * @Date 2021/1/15 6:09 下午
 */
@Component
public class ShutdownHookBean implements DisposableBean, BeanFactoryPostProcessor {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(ShutdownHookBean.class);

    public void init() {
        logger.info("ShutdownHookBean init.");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        ShutdownHookBean bean = beanFactory.getBean(ShutdownHookBean.class);
        bean.init();
    }

    @Override
    public void destroy() throws Exception {
        logger.info("ShutdownHookBean destroy start.");
        PlumeShutdownHook.destroyAll();
    }
}
