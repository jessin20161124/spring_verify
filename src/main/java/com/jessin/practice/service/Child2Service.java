package com.jessin.practice.service;

import com.jessin.practice.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zexin.guo
 * @create 2018-04-20 下午6:54
 **/
//@Service("2")
public class Child2Service extends AbstractService {
    Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public User getUserByName(String name) {
        logger.info("调用");
        return null;
    }
}
