package com.jessin.practice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Created by zexin.guo on 17-9-18.
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyBeanPostProcessor.class);
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        LOGGER.info("在BeanPostProcessor beforeInit调用前 className={}, beanName={}",
                bean, beanName);
//        System.out.println("<b>" + bean.getClass().getSimpleName() + "_" + beanName + "</b>");
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        LOGGER.info("在BeanPostProcessor afterInit调用后 className={}, beanName={}",
                bean, beanName);
        return bean;
    }
}
