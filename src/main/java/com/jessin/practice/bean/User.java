package com.jessin.practice.bean;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

/**
 * Created by zexin.guo on 17-8-5.
 */
// 必须注册为bean，settings属性文件和afterPropertiesSet才会起作用
@Component
public class User implements InitializingBean{
    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);
    private int id;
    private String[] car;
    // 默认支持yyyy/MM/dd的形式，DateTimeFormat不起作用，不知为何。
    // jsonformat必须配置时区，输出json时时间才会正确
     @DateTimeFormat(pattern="yyyy-MM-dd")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date birthday;
    // 不通过setter属性来注入，直接注入
    @Value("${name}")
//    @Value("#{config['name']}")
    private String name = "xiaoming"; // 构造实例时先设置xiaoming，之后被@Value("${name}")覆盖
    // 不用通过setter属性注入，直接注入，@Autowired也可以。
    @Value("${agent.list}")
    private String[] agentList;

    private Integer age;

    public User(){
        LOGGER.info("user构造函数初始化，name：{}", name);
    }

    public void setMoney(String money){
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

    public String[] getCar() {
        return car;
    }

    public void setCar(String[] car) {
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
    public String toString(){
        String ret = JSONObject.toJSONString(this);
        return ret;
    }

    @PostConstruct
    private void init(){
        LOGGER.info("postConstruct init user, name：{}", name);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("afterPropertiesSet_____：{}", this);
    }
}
