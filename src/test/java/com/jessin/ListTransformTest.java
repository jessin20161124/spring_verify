package com.jessin;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by zexin.guo on 17-10-19.
 */
public class ListTransformTest {
    private static final Logger logger = LoggerFactory.getLogger(ListTransformTest.class);

    @Test
    public void test1() {
        Function<String, String> function = new Function<String, String>() {
            public String apply(String s) {
                String ret = s.toUpperCase();
                logger.info("调用transform, {} -> {}", s, ret);
                return ret;
            }
        };
        List<String> list1 = Lists.newArrayList("hello", "world");
        List<String> list2 = Lists.transform(list1, function);
        for (String str : list2) {
            logger.info("第一次遍历：{}", str);
        }
        list2.get(0);
        // remove也会起作用
        list2.remove(0);
        list1.add("happy");
        for (String str : list2) {
            logger.info("第二次遍历：{}", str);
        }
    }

    @Test
    public void test2() {
        Function<String, String> function = new Function<String, String>() {
            public String apply(String s) {
                String ret = s.toUpperCase();
                logger.info("调用transform, {} -> {}", s, ret);
                return ret;
            }
        };
        List<String> list1 = Lists.newArrayList("hello", "world");
        List<String> list2 = Lists.newArrayList(Lists.transform(list1, function));
        for (String str : list2) {
            logger.info("第一次遍历：{}", str);
        }
        list2.get(0);
        // remove也会起作用
        list2.remove(0);
        list1.add("happy");
        for (String str : list2) {
            logger.info("第二次遍历：{}", str);
        }
    }
}
