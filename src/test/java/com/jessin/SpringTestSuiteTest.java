package com.jessin;

import com.jessin.practice.bean.User;
import com.jessin.practice.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * @author zexin.guo
 * @create 2019-01-27 下午3:23
 **/
@RunWith(SpringJUnit4ClassRunner.class)  //使用junit4进行测试
@ContextConfiguration({"/spring/app.xml"}) //加载配置文件
//------------如果加入以下代码，所有继承该类的测试类都会遵循该配置，也可以不加，在测试类的方法上///控制事务，参见下一个实例
//这个非常关键，如果不加入这个注解配置，事务控制就会完全失效！
//@Transactional
//这里的事务关联到配置文件中的事务控制器（transactionManager = "transactionManager"），同时//指定自动回滚（defaultRollback = true）。这样做操作的数据才不会污染数据库！
//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
//------------
public class SpringTestSuiteTest {
    @Resource
    private UserService userService;

    @Test
//    @Transactional   //标明此方法需使用事务
//    @Rollback(false)  //标明使用完此方法后事务不回滚,true时为回滚
    public void query() {
        User user = userService.query("小明");
        System.out.println(user);
        userService.updateAccount(user);
        userService.reload();
    }

    @Test
//    @Transactional   //标明此方法需使用事务
//    @Rollback(false)  //标明使用完此方法后事务不回滚,true时为回滚
    public void testAopNotValid() {
        User user = userService.getAccountByName2("小明");
        System.out.println(user);
    }
}
