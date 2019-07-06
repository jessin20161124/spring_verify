package com.jessin;

import com.jessin.practice.config.MyConfiguration;
import com.jessin.practice.service.HelloService;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author zexin.guo
 * @create 2019-05-03 下午8:11
 **/
public class AnnotationConfigApplicationContextTest {

    @Test
    public void test1() {
        AnnotationConfigApplicationContext annotationConfigApplicationContext
                = new AnnotationConfigApplicationContext(MyConfiguration.class);
        HelloService helloService
                = annotationConfigApplicationContext.getBean("getHelloService2", HelloService.class);
        helloService.greetTo("老师好");
    }
}
