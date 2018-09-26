package com.jessin;

import com.google.common.collect.Maps;
import com.jessin.practice.bean.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Map;

/**
 * @author zexin.guo
 * @create 2018-09-10 下午9:08
 **/
@Slf4j
public class BeanWrapperTest {

    @Test
    public void test1() {
        User user = new User();
        BeanWrapper beanWrapper = new BeanWrapperImpl(user);
        Map<String, String> userMap = Maps.newHashMap();
        userMap.put("id", "2013");
        userMap.put("age", "24");
        userMap.put("map[abc]", "信息");
        beanWrapper.setPropertyValues(userMap);
        log.info("结果为：{}", user);
    }
}
