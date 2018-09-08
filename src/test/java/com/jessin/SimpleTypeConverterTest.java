package com.jessin;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zexin.guo
 * @create 2018-09-06 下午8:13
 **/
public class SimpleTypeConverterTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private SimpleTypeConverter simpleTypeConverter = new SimpleTypeConverter();


    private class DatePropertyEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
            try {
                Date date = simpleDateFormat.parse(text);
                setValue(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testCustomPropertyEditor() {
        // 不注入时会报错
        simpleTypeConverter.registerCustomEditor(Date.class, new DatePropertyEditor());
        Date date = simpleTypeConverter.convertIfNecessary("2017-01-11", Date.class);
        logger.info("日期为：{}", date);
    }

    private class MyConversionService implements ConversionService {
        @Override
        public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
            if (sourceType == String.class && targetType == Date.class) {
                return true;
            }
            return false;
        }


        @Override
        public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
            return canConvert(sourceType.getType(), targetType.getType());
        }


        @Override
        public <T> T convert(Object source, Class<T> targetType) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
            try {
                Date date = simpleDateFormat.parse((String) source);
                return (T) date;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
            return convert(source, targetType.getType());
            // Stack over flow error...
//            return convert(source, sourceType, targetType);
        }
    }

    @Test
    public void testConversionService() {
        // 不注入时会报错
        simpleTypeConverter.setConversionService(new MyConversionService());
        Date date = simpleTypeConverter.convertIfNecessary("2017-01-11", Date.class);
        logger.info("日期为：{}", date);
    }
}
