package com.jessin;

import com.jessin.practice.bean.Friend;
import com.jessin.practice.service.HelloService;
import com.jessin.practice.service.impl.HelloServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author zexin.guo
 * @create 2018-08-01 上午7:46
 **/
@Slf4j
public class ApplicationContextTest {
    ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/spring/app.xml");

    @Test
    public void test1 () {
        Friend friend = applicationContext.getBean("friend", Friend.class);
        log.info("朋友为 : {}", friend);

//        // 类型不匹配，出错
//        Advisor friend = applicationContext.getBean("friend", Advisor.class);
//        log.info("朋友为 : {}", friend);
    }

    @Test
    public void test2 () {
        HelloService helloService = applicationContext.getBean(HelloService.class);
        log.info("HelloService为：" + helloService.getClass());
        helloService.hello();
        helloService.toString();
        log.info("HelloService : {}", helloService instanceof Advised);
        log.info("HelloService : {}", helloService instanceof SpringProxy);
        log.info("HelloService : {}", helloService instanceof HelloServiceImpl);
    }
}
