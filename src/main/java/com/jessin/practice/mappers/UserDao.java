package com.jessin.practice.mappers;

import com.jessin.practice.bean.User;
import org.springframework.stereotype.Repository;

/**
 * @author zexin.guo
 * @create 2019-01-14 下午7:10
 **/
@Repository
public interface UserDao {
    User selectUser();
}
