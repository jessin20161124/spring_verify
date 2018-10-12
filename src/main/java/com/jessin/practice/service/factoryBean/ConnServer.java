package com.jessin.practice.service.factoryBean;

import com.jessin.practice.bean.User;

/**
 * @author zexin.guo
 * @create 2018-10-10 下午8:00
 **/

public class ConnServer {
    public User connWorld() {
        User user = new User();
        user.setAge(10);
        user.setName("小欣欣");
        return user;
    }
}
