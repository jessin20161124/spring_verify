package com.jessin;

import com.jessin.practice.bean.Friend;
import com.jessin.practice.bean.User;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by zexin.guo on 17-8-20.
 */
public class UserTest {

    @Test
    public void testMockClass(){
        User mockUser = spy(User.class);
        // 指定返回10，原本返回的100被覆盖了
        when(mockUser.getId()).thenReturn(10);
        // 测试通过
        assertEquals(10, mockUser.getId());
    }

    @Test
    public void testVerify(){
        Friend friend = new Friend();
//        friend.setMyFriends(new ArrayList<User>());
//        User mockUser = mock(User.class);
//        // 如果不设置，返回null
//        when(mockUser.getName()).thenReturn("jessin");
//        friend.getMyFriends().add(mockUser);
//        // 照常打印名字
//        friend.printMyFriends();
//
//        // 验证该方法被调用一次
//        verify(mockUser).getName();
    }
}
