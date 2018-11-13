package com.jessin.practice.bean;

import com.jessin.practice.annotation.HeritedAnnotation;

import java.util.Date;
import java.util.List;

/**
 * Created by zexin.guo on 17-8-21.
 */
@HeritedAnnotation("hello")
public class Friend {
    private Integer id;
    private String name;
    private List<User> user;
    private List<Date> date;
//    public int getId(){
//        return id; // 拆箱。实际执行integer.intValue()。如果integer为null，则抛出null异常。
//    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUser() {
        return user;
    }

    public void setUser(List<User> user) {
        this.user = user;
    }

    public List<Date> getDate() {
        return date;
    }

    public void setDate(List<Date> date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", user=" + user +
                ", date=" + date +
                '}';
    }

    public static void main(String[] args){
        new Friend().getId();
    }
}
