package com.jessin.practice.bean;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zexin.guo on 17-8-5.
 */
// 必须注册为bean，settings属性文件和afterPropertiesSet才会起作用
@Component
public class User implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);
    private int id;
    private List<String> car;
    // jsonformat必须配置时区，输出json时时间才会正确
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date birthday;
    // 不通过setter属性来注入，直接注入
//    @Value("${name}")
//    @Value("#{config['name']}")
//    @NotBlank(message = "名字不能为空啊")
    private String name; // 构造实例时先设置xiaoming，之后被@Value("${name}")覆盖
    // 不用通过setter属性注入，直接注入，@Autowired也可以。
    @Value("${agent.list}")
    private String[] agentList;

    @NotNull(message = "age不能为null")
    private Integer age;

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    // 需要初始化，才能注入map[abc][def]
    private Map<String, String> map = Maps.newHashMap();

    public User() {
        LOGGER.info("user构造函数初始化，name：{}", name);
    }

    public void setMoney(String money) {
        System.out.println("money=" + money);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        LOGGER.info("设置name");
        this.name = name;
    }

    public List<String> getCar() {
        return car;
    }

    public void setCar(List<String> car) {
        LOGGER.info("设置car");
        this.car = car;
    }

    public String[] getAgentList() {
        return agentList;
    }

    public void setAgentList(String[] agentList) {
        this.agentList = agentList;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }


    @Override
    public String toString() {
        String ret = JSONObject.toJSONString(this);
        return ret;
    }

    @PostConstruct
    private void init() {
        LOGGER.info("postConstruct init user, name：{}", name);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("afterPropertiesSet_____：{}", this);
    }
}
