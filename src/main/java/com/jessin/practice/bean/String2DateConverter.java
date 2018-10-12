package com.jessin.practice.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zexin.guo
 * @create 2018-09-06 下午9:06
 **/
public class String2DateConverter implements Converter<String, Date> {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public Date convert(String source) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = simpleDateFormat.parse(source);
            logger.info("使用String2DateConverter格式化日期：{}，结果为：{}", source, date);
            return date;
        } catch (ParseException e) {
            logger.error("解析出错了：{}", source, e);
            return null;
        }
    }
}
