package com.jessin.practice.service.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zexin.guo
 * @create 2018-10-11 下午5:30
 **/
@Service
@Slf4j
public class BeanB {
    @Resource
    private BeanC beanC;

    public void print() {
        log.info("I am beanB");
        beanC.print();
    }
}
