package com.jessin.practice.service;

import com.jessin.practice.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zexin.guo
 * @create 2017-11-12 下午4:10
 **/
//@Service("1")
public class ChildService extends AbstractService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public User getUserByName(String name) {
        logger.info("获取用户：{}", name);
        return getUser();
    }
}
