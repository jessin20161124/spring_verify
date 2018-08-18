package com.jessin.practice.service;

import com.jessin.practice.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;

/**
 * @author zexin.guo
 * @create 2017-11-12 下午4:09
 **/
public abstract class AbstractService implements InitializingBean {
    private Logger logger = LoggerFactory.getLogger(AbstractService.class);
    @Resource
    private User user;

    protected User getUser() {
        logger.info("父类中获取用户");
        return user;
    }

    public abstract User getUserByName(String name);

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("调用afterPropertiesSet方法");
    }
}
