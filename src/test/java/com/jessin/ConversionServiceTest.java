package com.jessin;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.support.DefaultFormattingConversionService;

import java.util.List;

/**
 * @author zexin.guo
 * @create 2018-09-09 下午11:58
 **/
@Slf4j
public class ConversionServiceTest {
    private ConversionService conversionService = new DefaultFormattingConversionService();

    @Test
    public void test1() {
        String[] arrays = {"a", "b"};
        TypeDescriptor sourceType = TypeDescriptor.forObject(arrays);
        TypeDescriptor listType = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class));
        // 转化Object[] -> Collection
        boolean result = conversionService.canConvert(sourceType, listType);
        Object obj = conversionService.convert(arrays, sourceType, listType);
        log.info("可以转换：{}，转换结果为：{}，所在类为：{}", result, obj, obj.getClass());
    }
}
