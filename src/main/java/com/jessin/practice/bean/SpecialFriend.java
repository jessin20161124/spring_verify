package com.jessin.practice.bean;

import com.jessin.practice.annotation.HeritedAnnotation;

/**
 * @author zexin.guo
 * @create 2018-11-12 下午9:12
 **/
public class SpecialFriend extends Friend {
    public static void main(String[] args) {
        System.out.println(SpecialFriend.class.getAnnotation(HeritedAnnotation.class));
    }
}
