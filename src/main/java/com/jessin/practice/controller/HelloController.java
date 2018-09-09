package com.jessin.practice.controller;

import com.google.common.collect.Maps;
import com.jessin.practice.bean.User;
import com.jessin.practice.event.HelloEvent;
import com.jessin.practice.service.AbstractService;
import com.jessin.practice.service.ChildService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by jessin on 17-7-22.
 */
@Controller
public class HelloController {

    @Resource
    private List<AbstractService> abstractServiceList;
    @Resource
    private Map<String, AbstractService> abstractServiceMap;

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    private class UserEditor extends PropertyEditorSupport {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {

            if (text.indexOf(":") == -1) {
                throw new RuntimeException("test不包含有:" + text);
            }
            String[] infos = text.split(":");
            User user = new User();
            user.setId(Integer.parseInt(infos[0]));
            user.setName(infos[1]);
            //  user.setBirthday(new Date());
            ArrayList userList = new ArrayList();
            userList.add(user);
            setValue(userList);
        }
    }

    @Value("${name}")
    private String name;

    @Resource
    private ChildService abstractService;
    @Resource
    private ApplicationContext applicationContext;

    // 日期入参格式化
    @InitBinder
    public void init(WebDataBinder dataBinder) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        CustomDateEditor dateEditor = new CustomDateEditor(dateFormat, true);
//        dataBinder.registerCustomEditor(Date.class, dateEditor);
        //dataBinder.registerCustomEditor(List.class, new UserEditor());
    }

    /**
     * 使用@RequestParam注解，表示使用RequestParamMethodHandlerResolver进行处理
     * 否则使用兜底的ModelAttributeHandlerResolver进行处理
     *
     * 另外，返回值如果没有标注@ResponseBody，则使员工ModelAttributeHandlerResolver进行解析，
     * 这时会把返回值User放到ModelAndView中，但是没有View，所以会使用默认的view，也就是该url；
     * 由于没有注入视图解析器，默认从dispatchServlet.properties中获取到InternalResourceViewResolver得到
     * InternalResourceView，最后render时，由于资源不存在，所以出错了。。
     * @param user
     * @return
     */
    @RequestMapping("/sayUser")
    @ResponseBody
    public User sayUser(@RequestParam User user) {
        return user;
    }

//    @RequestMapping(value = "/hello", params = "car=123")

    @RequestMapping(value = "/hello", produces = "application/json; charset=UTF-8")
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    // 可以通过注入数组，调用set方法设置属性。
    // curl 'http://localhost:8081/practice/hello?name=tom&car=1342&car=13412' --data-urlencode 'car=我爱你'
    public Map<String, Object> sayHello(User user, String abc) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            //user.setName(null);
            LOGGER.info("name ：{}", name);
//            applicationContext.publishEvent(new HelloEvent(user.getBirthday()));
        } catch (Exception e) {
            LOGGER.error("error {}", abstractServiceList.get(0).getUserByName("小明"), e);
            throw new RuntimeException(e);
        }
        Map<String, Object> map = new HashMap();
        map.put("user", user);
        map.put("abc", abc);
        LOGGER.info(".................发布事件，map为:{}..........................", map);
        LOGGER.info("获取到带有所有的Controller bean：{}", applicationContext.getBeansWithAnnotation(Controller.class));
        LOGGER.info("获取RequestMappingHandlerAdapter所有实例的beanName : {}", applicationContext.getBeanNamesForType(RequestMappingHandlerAdapter.class));

        applicationContext.publishEvent(new HelloEvent("say hello"));
        applicationContext.publishEvent("just string");
        return map;
    }

    @RequestMapping(value = "/hello", params = "name=tom")
    @ResponseBody
    // 可以通过注入数组，调用set方法设置属性。
    // curl 'http://localhost:8081/practice/hello?name=tom&car=1342&car=13412' --data-urlencode 'car=我爱你'
    public Map<String, Object> sayWorld() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            //user.setName(null);
            LOGGER.info("name ：{}", name);
//            applicationContext.publishEvent(new HelloEvent(user.getBirthday()));
        } catch (Exception e) {
            LOGGER.error("error {}", abstractServiceList.get(0).getUserByName("小明"), e);
            throw new RuntimeException(e);
        }
        Map<String, Object> map = new HashMap();
        User user = new User();
        user.setId(1);
        user.setBirthday(new Date());
        user.setName("小妹");
        map.put("user", user);
        map.put("abc", null);
        LOGGER.info(".................发布事件..........................");
        LOGGER.info("获取到带有所有的Controller bean：{}", applicationContext.getBeansWithAnnotation(Controller.class));
        applicationContext.publishEvent(new HelloEvent("say hello"));
        applicationContext.publishEvent("just string");
        return map;
    }

    @RequestMapping("/showUserListByFtl")
    public String showUserListInFtl(ModelMap mm) {
        List<User> list = new ArrayList<User>(0);
        User user1 = new User();
        user1.setId(1);
//        user1.setBirthday(new Date());
        user1.setName("tony");
        list.add(user1);

        User user2 = new User();
        user2.setId(2);
//        user2.setBirthday(new Date());
        user2.setName("amy");
        list.add(user2);
        mm.addAttribute("userList", list);
        return "userListFtl";
    }

    /**
     * ViewNameMethodReturnValueHandler进行处理，返回的字符串即为视图名，而@ModelAttribute会把参数放入model中，ｋey为user
     * @param user
     * @return
     */
    @RequestMapping("/showUserListByJsp")
    public String showUserByJsp(@ModelAttribute User user) {
        return "showUserListByJsp";
    }

    /**
     * 由StringHttpMessageConverter处理，abc含有中文时，将是乱码，因为返回值使用ISO8859-1编码，而前端使用UTF-8编码
     * @param abc
     * @return
     */
    @RequestMapping("/sayHello")
    @ResponseBody
    public String sayHello(String abc) {
        return abc;
    }

    @RequestMapping(value = "/fetchImage/{fileName}", produces = "application/octet-stream")
    @ResponseBody
    public byte[] getImage(@PathVariable("fileName") String fileName) {
        // TODO 改用ClassPathResource
        Path path = Paths.get(this.getClass().getResource("/" + fileName).getPath());
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    @PostConstruct
    public void init() {
        LOGGER.error("map的大小为：{}, {}", abstractServiceMap.size(), abstractServiceMap);
    }

    public static void main(String[] args) {
        ResponseBody responseBody = AnnotationUtils.findAnnotation(HelloController.class, ResponseBody.class);
        Controller controller = AnnotationUtils.findAnnotation(HelloController.class, Controller.class);
        LOGGER.info("注解为：{}, {}", responseBody, controller);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Map<String, Object> processException(MissingServletRequestParameterException exception, HandlerMethod handlerMethod) {
        LOGGER.info(handlerMethod + "抛出特别的异常了啊 : " + exception.getMessage());
        Map<String, Object> map = Maps.newHashMap();
        map.put("error", "抛出异常了");
        return map;
    }
}
