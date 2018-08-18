package com.jessin;

import com.jessin.practice.bean.Friend;
import com.jessin.practice.service.ChildService;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.beans.PropertyDescriptor;

/**
 * @author zexin.guo
 * @create 2018-07-22 下午3:50
 **/
public class BeanFactoryTest {

    /**
     * 获取默认的构造函数：
     * clazz.getDeclaredConstructor((Class[]) null);
     */
    @Test
    public void test1() {
        Resource resource = new ClassPathResource("/spring/app.xml");
        XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(resource);
        // 尝试一下factory method name
        xmlBeanFactory.addBeanPostProcessor(new InstantiationAwareBeanPostProcessor() {

            @Override
            public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
                System.out.println("进入实例化后处理器");
                // 返回null，才能继续往下走
                return null;
            }

            @Override
            public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
                // true继续往下走，false不再往下走
                return true;
            }

            @Override
            public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
                return pvs;
            }

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                // 返回Null的话，最后的bean会变为Null，遇到null时立即返回，不再运行其他BeanPostProcessor
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }
        });
        CommonAnnotationBeanPostProcessor commonAnnotationBeanPostProcessor = new CommonAnnotationBeanPostProcessor();
        commonAnnotationBeanPostProcessor.setBeanFactory(xmlBeanFactory);
        xmlBeanFactory.addBeanPostProcessor(commonAnnotationBeanPostProcessor);
        Friend friend = xmlBeanFactory.getBean("goodFriend", Friend.class);
//        InjectionService injectionService = xmlBeanFactory.getBean(InjectionService.class);
//        injectionService.add();
//        System.out.println(injectionService);
    }

    @Test
    public void test2() {
        Class<ChildService> childServiceClass = ChildService.class;
        try {
            // 得到默认的构造函数
            System.out.println(childServiceClass.getDeclaredConstructor((Class[]) null));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
