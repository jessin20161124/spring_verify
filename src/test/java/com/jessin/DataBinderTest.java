package com.jessin;

import com.google.common.collect.Maps;
import com.jessin.practice.bean.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.PropertyValues;
import org.springframework.validation.DataBinder;

import java.util.Map;

/**
 * @author zexin.guo
 * @create 2018-10-11 下午2:46
 **/
@Slf4j
public class DataBinderTest {

    @Test
    public void testWithoutTarget1() {
        DataBinder dataBinder = new DataBinder(null);
        Integer myInt = 10;
        String str = dataBinder.convertIfNecessary(myInt, String.class);
        log.info("str : {}，bindingResult : {}", str, dataBinder.getBindingResult());
    }

    @Test
    public void testWithoutTarget2() {
        DataBinder dataBinder = new DataBinder("a");
        Integer myInt = 10;
        Map<String, Integer> propertyMap = Maps.newHashMap();
        propertyMap.put("myInt", 10);
        // target为null时，不能调用bind
        PropertyValues propertyValues = new MutablePropertyValues(propertyMap);
        dataBinder.bind(propertyValues);
        log.info("bindingResult : {}", dataBinder.getBindingResult());
    }


    @Test
    public void testWithTarget() {
        User user = new User();
        DataBinder dataBinder = new DataBinder(user);
        ((PropertyEditorRegistrySupport)dataBinder.getBindingResult().getPropertyEditorRegistry()).useConfigValueEditors();
        Map<String, Object> propertyMap = Maps.newHashMap();
        propertyMap.put("car", new String[]{"小车","汽车"});
        propertyMap.put("agentList", "haha,baba");
        PropertyValues propertyValues = new MutablePropertyValues(propertyMap);
        dataBinder.bind(propertyValues);
        log.info("bindingResult : {}，结果：{}", dataBinder.getBindingResult(), user);
    }
}