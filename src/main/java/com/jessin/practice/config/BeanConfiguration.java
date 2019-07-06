package com.jessin.practice.config;

import com.jessin.practice.service.HelloService;
import com.jessin.practice.service.impl.HelloServiceImpl;
import org.springframework.context.annotation.Bean;

/**
 * @author zexin.guo
 * @create 2019-05-03 下午9:30
 **/
public class BeanConfiguration {

    @Bean
    public HelloService getHelloService2() {
        return new HelloServiceImpl();
    }
}
