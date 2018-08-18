package com.jessin;

import com.jessin.practice.service.AbstractService;
import com.jessin.practice.service.Child2Service;
import com.jessin.practice.service.ChildService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zexin.guo
 * @create 2018-07-16 上午9:22
 **/
public class ClassTest {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test1() {
        Class clazz1 = Child2Service.class;
        // 判断是否可以赋值
        logger.info("{}", AbstractService.class.isAssignableFrom(clazz1));
    }

    @Test
    public void test2() {
        ChildService childService = new ChildService();
        Child2Service child2Service = new Child2Service();
        logger.info("{}", AbstractService.class.isInstance(childService));
        logger.info("{}", AbstractService.class.isInstance(child2Service));

    }
}
