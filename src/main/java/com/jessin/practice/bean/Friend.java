package com.jessin.practice.bean;

/**
 * Created by zexin.guo on 17-8-21.
 */
public class Friend {
    private Integer id;
    private String name;
    private User user;
    public int getId(){
        return id; // 拆箱。实际执行integer.intValue()。如果integer为null，则抛出null异常。
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", user=" + user +
                '}';
    }

    public static void main(String[] args){
        new Friend().getId();
    }
}
