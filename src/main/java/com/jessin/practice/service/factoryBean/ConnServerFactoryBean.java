package com.jessin.practice.service.factoryBean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Service;

/**
 * @author zexin.guo
 * @create 2018-10-10 下午7:58
 **/
@Service
public class ConnServerFactoryBean implements FactoryBean<ConnServer> {
    ConnServer connServer = new ConnServer();
    @Override
    public ConnServer getObject() throws Exception {
        return connServer;
    }

    @Override
    public Class<?> getObjectType() {
        return ConnServer.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
