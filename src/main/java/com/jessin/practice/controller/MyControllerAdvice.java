package com.jessin.practice.controller;

import com.google.common.collect.Maps;
import com.jessin.practice.bean.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;

import java.beans.PropertyEditorSupport;
import java.util.Map;

/**
 * 先运行ControllerAdvice中的InitBinder，再初始化Controller中的InitBinder，可以被覆盖
 * 限制起作用的包
 * @author zexin.guo
 * @create 2018-09-08 上午10:55
 **/
@Slf4j
@ControllerAdvice(basePackages = "com.jessin.practice.controller")
public class MyControllerAdvice {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @InitBinder
    public void globalInit(WebDataBinder webDataBinder) {
        logger.info("创建DataBinder时调用MyControllerAdvice的InitBinder初始化方法来初始化DataBinder");
        webDataBinder.registerCustomEditor(User.class, new PropertyEditorSupport() {
            /**
             * convertIfNecessary时，转化为对应的实例！传递个class就可以实例化了
             * @param text
             * @throws IllegalArgumentException
             */
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                String[] items = text.split(":");
                if (items.length != 3) {
                    throw new IllegalArgumentException("参数非法：" + text + "，格式必须为id:name:age哈");
                }
                User user = new User();
                user.setId(Integer.parseInt(items[0]));
                user.setName(items[1]);
                user.setAge(Integer.parseInt(items[2]));
                setValue(user);
            }
        });
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, Object> processException(Exception exception, HandlerMethod handlerMethod) {
        log.info(handlerMethod + "抛出特别的异常了啊 : " + exception.getMessage());
        Map<String, Object> map = Maps.newHashMap();
        map.put("error", exception.getMessage());
        return map;
    }
}
