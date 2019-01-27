package com.jessin.practice.service;

import com.jessin.practice.bean.User;
import com.jessin.practice.mappers.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Cache的实现见
 * @see org.springframework.cache.interceptor.CacheInterceptor
 * 相关的Link ：https://www.cnblogs.com/zsychanpin/p/7191021.html
 * @author zexin.guo
 * @create 2019-01-26 下午10:29
 **/
@Service
@Slf4j
public class UserService {

    @Resource
    private UserDao userDao;

    /**
     * TODO 内部引用，AOP proxy query将失效...
     * @param accountName
     * @return
     */
    public User getAccountByName2(String accountName) {
        return this.query(accountName);
    }

    /**
     * 使用了一个缓存名叫accountCache，默认key为name参数的值
     * @return
     */
    @Cacheable(value="accountCache")
    public User query(String name) {
        log.info("从db查找user");
        return userDao.selectUser();
    }

    /**
     * 逐出该key
     * 更新成功才更新到缓存中
     * @param user
     */
    @CacheEvict(value="accountCache",key="#user.getName()")
    public void updateAccount(User user) {
//        updateDB(user);
        log.info("更新完成后再逐出该key");
    }

    /**
     * value都是cache的名称
     * reload即为清空操作
     */
    @CacheEvict(value="accountCache",allEntries=true)
    public void reload() {
        log.info("逐出所有的key");
    }

    /**
     * 调用这个方法，并将返回值更新到缓存中
     * @param user
     * @return
     */
    @CachePut(value="accountCache",key="#user.getName()")
    public User updateAccount2(User user) {
        log.info("更新db后再更新缓存");
        return user;
    }
}
