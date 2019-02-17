package com.jessin;

import org.junit.Test;

/**
 * @author zexin.guo
 * @create 2019-02-04 下午4:11
 **/
public class RegexTest {

    @Test
    public void test1() {
        String regex = "\\?|\\*|\\{((?:\\{[^/]+?\\}|[^/{}]|\\\\[{}])+?)\\}";
        System.out.println(regex);
    }
}
