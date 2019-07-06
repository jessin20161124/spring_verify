package com.jessin.practice.service.impl;

import com.jessin.practice.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author zexin.guo
 * @create 2018-09-26 下午5:45
 **/
@Service
@Slf4j
public class HelloServiceImpl implements HelloService {
    @Override
    public void hello() {
        log.info("运行hello");
    }

    @Override
    public void greetTo(String name) {
        log.info("向{}打招呼", name);
    }
}



