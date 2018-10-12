package com.jessin.practice.service.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zexin.guo
 * @create 2018-10-11 下午5:31
 **/
@Service
@Slf4j
public class BeanD {
    public void print() {
        log.info("I am beanD");
    }
}
