package com.jessin;

import com.google.common.collect.Maps;
import com.jessin.practice.bean.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.Map;

/**
 * @author zexin.guo
 * @create 2018-09-12 上午7:25
 **/
@Slf4j
public class BeanPropertyBindingResultTest {

    @Test
    public void test1() {
        User targetUser = new User();

        BeanPropertyBindingResult result = new BeanPropertyBindingResult(targetUser,
                "targetUser", true, 20);
        result.initConversion(new DefaultFormattingConversionService());

        Map<String, String> userMap = Maps.newHashMap();
        userMap.put("id", "2013");
        userMap.put("age", "24");
        userMap.put("name", "信息");
        userMap.put("birthday", "2019-01-019");
        // result.getPropertyAccessor返回的是BeanWrapper
        result.getPropertyAccessor().setPropertyValues(userMap);

        log.info("参数绑定，结果为：{}", targetUser);
    }
}
