package com.jessin.practice.service.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zexin.guo
 * @create 2018-10-11 下午5:30
 **/
@Service
@Scope("prototype")
@Slf4j
public class BeanA {
    @Resource
    private BeanB beanB;

    private double random = Math.random();

    public void print() {
        log.info("I am Bean A，random is {}", random);
        beanB.print();
    }
}
