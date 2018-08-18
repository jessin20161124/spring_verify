package com.jessin;

import org.junit.Test;

import java.util.LinkedHashSet;

/**
 * @author zexin.guo
 * @create 2018-07-16 下午8:09
 **/
public class LinkedHashSetTest {

    @Test
    public void test1() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add("hello");
        set.add("world");
        set.add("abc");
        set.add("world");
        set.add("hello");
        System.out.println(set);
    }
}
