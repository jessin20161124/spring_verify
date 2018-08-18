package com.jessin;

import com.jessin.practice.bean.User;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author zexin.guo
 * @create 2018-08-01 上午7:46
 **/
public class ApplicationContextTest {
    @Test
    public void test1 () {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/spring/app.xml");
        User user = (User)applicationContext.getBean("user");
        System.out.println(user);
    }
}
