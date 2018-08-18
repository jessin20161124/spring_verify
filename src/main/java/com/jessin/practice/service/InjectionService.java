package com.jessin.practice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author zexin.guo
 * @create 2018-07-29 下午8:31
 **/
@Service
public class InjectionService {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private List<AbstractService> abstractServiceList;
    @Resource
    private Map<String, AbstractService> abstractServiceMap;

    public void add() {
        logger.info("两个类为：{}" + abstractServiceList);
    }
}
