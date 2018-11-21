package com.jessin.practice.config;

import com.jessin.practice.service.Child2Service;
import com.jessin.practice.service.ChildService;
import com.jessin.practice.service.HelloService;
import com.jessin.practice.service.impl.HelloServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author zexin.guo
 * @create 2018-11-19 下午7:42
 **/

@Configuration
// propertyResource只会将属性注入到当前环境中，只能从当前环境中取。
@PropertySource("test1.properties")
//@ImportResource("/abc.xml")
//@Import(MyConfiguration.class)
@Slf4j
public class MyConfiguration {

    private Child2Service child2Service;

    private ChildService childService;

    /**
     * 从值解析器中取值，如果有PropertySourcesPlaceholderConfigurer(
     * <context:property-placeholder location="classpath*:/test.properties,classpath*:jdbc.properties" />)，则会先从环境变量中取，再从对应配置文件中取
     */
    @Value("${myName}")
    private String name;

    @Resource
    private Environment environment;

    /**
     * 入参一般用在new HelloServiceImpl的构造函数中
     * 参数属性自动注入
     * @param childService
     * @param child2Service
     * @return
     */
    @Bean
    public HelloService getHelloService(ChildService childService, Child2Service child2Service) {
        this.child2Service = child2Service;
        this.childService = childService;
        log.info("configuration注入：{}，{}，name为：{}", childService, child2Service, name);
        return new HelloServiceImpl();
    }

    @PostConstruct
    public void init() {
        log.info("configuration注入，name为：{}，getName为：{}", name, environment.getProperty("myName"));
    }
}
