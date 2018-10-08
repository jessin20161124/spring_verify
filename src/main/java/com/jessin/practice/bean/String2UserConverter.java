package com.jessin.practice.bean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;

/**
 * TODO 对于ConversionService用的是Converter
 * @author zexin.guo
 * @create 2018-09-11 上午7:43
 **/
@Slf4j
public class String2UserConverter implements Converter<String, User> {

    /**
     * Convert the source object of type {@code S} to target type {@code T}.
     *
     * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
     * @return the converted object, which must be an instance of {@code T} (potentially {@code null})
     * @throws IllegalArgumentException if the source cannot be converted to the desired target type
     */
    @Override
    public User convert(String source) {
        String[] items = source.split(":");
        if (items.length != 3) {
            throw new IllegalArgumentException("参数非法：" + source + "，格式必须为id:name:age哦");
        }
        User user = new User();
        user.setId(Integer.parseInt(items[0]));
        user.setName(items[1]);
        user.setAge(Integer.parseInt(items[2]));
        log.info("使用" + getClass().getSimpleName() + "转化user");
        return user;
    }
}
