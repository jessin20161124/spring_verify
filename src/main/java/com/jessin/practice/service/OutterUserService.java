package com.jessin.practice.service;

import com.jessin.practice.bean.User;
import com.jessin.practice.mappers.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 同一个类内部调用事务不生效
 * @Author: zexin.guo
 * @Date: 19-10-20 下午10:25
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 */
@Service
@Slf4j
public class OutterUserService {
    @Resource
    private UserDao userDao;

    @Resource
    private USerService uSerService;

    /**
     * 试一下序列化读
     * 事务中多次查询以第一次查询为准，如果中间数据发生变化的话，一直不变，保证可重复读...
     * @param userName
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public void transaction1(String userName) {
        User user1 = userDao.selectUserByName(userName);
        log.info("第一次查询用户：{}，结果为：{}", userName, user1);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        User user2 = userDao.selectUserByName(userName);
        log.info("第二次查询用户：{}，结果为：{}", userName, user2);
        user2.setNote("哈哈哈");
        int updateResult = uSerService.updateAccount(user2, user2.getAge());
        log.info("基于老数据更新结果为：{}", updateResult);
        //throw new RuntimeException("异常拉");
    }
}
