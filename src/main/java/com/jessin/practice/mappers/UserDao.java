package com.jessin.practice.mappers;

import com.jessin.practice.bean.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author zexin.guo
 * @create 2019-01-14 下午7:10
 **/
@Repository
public interface UserDao {
    User selectUserByName(String name);

    int updateUserByName(@Param("user") User user,
            @Param("oldAge") Integer oldAge);

    int insertUser(User user);

}
