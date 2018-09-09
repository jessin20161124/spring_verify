package com.jessin;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;

/**
 * @author zexin.guo
 * @create 2018-09-08 下午3:58
 **/
@Slf4j
public class HandlerAdapterOrderTest {

    @Test
    public void test1() {
        RequestMappingHandlerAdapter requestMappingHandlerAdapter1 = new RequestMappingHandlerAdapter();
        requestMappingHandlerAdapter1.setOrder(1);

        RequestMappingHandlerAdapter requestMappingHandlerAdapter2 = new RequestMappingHandlerAdapter();

        List<RequestMappingHandlerAdapter> requestMappingHandlerAdapterList =
                Lists.newArrayList(requestMappingHandlerAdapter2, requestMappingHandlerAdapter1);

        log.info("排序前结果：{}", requestMappingHandlerAdapterList);
        AnnotationAwareOrderComparator.sort(requestMappingHandlerAdapterList);
        log.info("排序后结果：{}", requestMappingHandlerAdapterList);
    }
}
