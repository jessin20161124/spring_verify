package com.jessin.practice.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jessin.practice.bean.Friend;
import com.jessin.practice.bean.User;
import com.jessin.practice.event.HelloEvent;
import com.jessin.practice.service.AbstractService;
import com.jessin.practice.service.ChildService;
import com.jessin.practice.service.UserService;
import com.jessin.practice.service.factoryBean.ConnServer;
import com.jessin.practice.service.test.BeanA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.AsyncTaskMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.CallableMethodReturnValueHandler;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by jessin on 17-7-22.
 */
@Controller
//@Scope("prototype")
public class HelloController {

    @Resource
    private List<AbstractService> abstractServiceList;
    @Resource
    private Map<String, AbstractService> abstractServiceMap;

    @Resource
    private UserService userService;

    @Resource
    private ConnServer connServer;

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
    public void localInit(WebDataBinder dataBinder) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        CustomDateEditor dateEditor = new CustomDateEditor(dateFormat, true);
//        dataBinder.registerCustomEditor(Date.class, dateEditor);
        //dataBinder.registerCustomEditor(List.class, new UserEditor());
    }

    @RequestMapping("/connServer")
    @ResponseBody
    public User connServer() {
        return connServer.connWorld();
    }

    /**
     * 使用@RequestParam注解，表示使用RequestParamMethodHandlerResolver进行处理
     * 否则使用兜底的ModelAttributeHandlerResolver进行处理
     *
     * 另外，返回值如果没有标注@ResponseBody，则使用ModelAttributeHandlerResolver进行解析，
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

    /**
     * TODO 拦截器和过滤器均需添加异步支持： <async-supported>true</async-supported>，且servlet为3.0及以上
     * 提前释放当前线程，交由其他线程处理复杂的任务，其他线程处理得到结果后，触发另一个线程将结果返回给浏览器，
     * 这一过程中response一直打开。
     * 在返回值处理器中处理
     * @see CallableMethodReturnValueHandler
     * @param user
     * @return
     */
    @RequestMapping("/callableSayUser")
    @ResponseBody
    public Callable<User> callableSayUser(@RequestParam User user) {
        return new Callable<User>() {
            @Override
            public User call() throws Exception {
                Thread.sleep(3000);
                return user;
            }
        };
    }

    /**
     * http://localhost:8081/practice/asyncTaskSayUser?user=1:ming:2
     * 提前释放当前线程，交由其他线程处理复杂的任务，其他线程处理得到结果后，出发另一个线程将结果返回给浏览器，
     * 这一过程中response一直打开。
     * @see AsyncTaskMethodReturnValueHandler 这个可以设置本地超时时间
     * @param user
     * @return
     */
    @RequestMapping("/asyncTaskSayUser")
    @ResponseBody
    public WebAsyncTask<User> asyncTaskSayUser(@RequestParam User user) {
        // 和配置中同时设置时，以这个为准。
        WebAsyncTask webAsyncTask = new WebAsyncTask<User>(20000, new Callable<User>() {
            @Override
            public User call() throws Exception {
                Thread.sleep(4000);
                return user;
            }
        });
        List<String> a = Lists.newArrayList();
        webAsyncTask.onTimeout(new Callable() {
            @Override
            public Object call() throws Exception {
                Map<String, Object> map = Maps.newHashMap();
                map.put("error", "超时了");
                map.put("ret", false);
                return map;
            }
        });
        return webAsyncTask;
    }

    /**
     * 外部是接口List，不可实例化，ModelAttributeMethodProcessor#createAttribute处理时会出错
     * @param user
     * @return
     */
//    @RequestMapping("/sayUserList")
//    @ResponseBody
//    public List<User> sayUserList(List<User> user) {
//        return user;
//    }

    /**
     * curl -X POST -H "content-type: application/json;charset=utf-8" -d '[{"id":1,"birthday":"2018-09-29 20:07:00"},{"id":2,"birthday":"2018-09-29 20:07:00"}]}' http://localhost:8081/practice/sayUserList
     * @param user
     * @return
     */
    @RequestMapping("/sayUserList")
    @ResponseBody
    public List<User> sayUserList(@RequestBody List<User> user) {
        return user;
    }

    /**
     * 外部是接口List，不可实例化，ModelAttributeMethodProcessor#createAttribute处理时会出错
     * @param friend
     * @return
     */
    @RequestMapping("/sayFriend")
    @ResponseBody
    public Friend sayFriend(Friend friend) {
        return friend;
    }

//    @RequestMapping(value = "/hello", params = "car=123")

    /**
     * TODO bindingResult跟随在@Valid后面，且必须有，对应的参数解析器为：org.springframework.web.method.annotation.ErrorsMethodArgumentResolver
     * grep 'propertyName' --color=auto -C 50 log/spring_verify.log
     * @param user
     * @param abc
     * @return
     */
    @RequestMapping(value = "/hello", produces = "application/json; charset=UTF-8")
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    // 可以通过注入数组，调用set方法设置属性。
    // curl 'http://localhost:8081/practice/hello?car=1342&car=13412' --data-urlencode 'car=我爱你'
    public Map<String, Object> sayHello(@Valid User user, BindingResult bindingResult, String abc) {
        Map<String, Object> map = new HashMap();

        if (bindingResult != null && bindingResult.hasFieldErrors()) {
            String errorMsg = "";
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                errorMsg = errorMsg + fieldError.getDefaultMessage() + ",";
            }
            map.put("error", bindingResult.getAllErrors());
            map.put("errorMsg", errorMsg);
            return map;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            //user.setName(null);
            LOGGER.info("name ：{}", name);
//            applicationContext.publishEvent(new HelloEvent(user.getBirthday()));
        } catch (Exception e) {
            LOGGER.error("error {}", abstractServiceList.get(0).getUserByName("小明"), e);
            throw new RuntimeException(e);
        }
        map.put("user", user);
        map.put("abc", abc);
        map.put("bindingResult", bindingResult);
        LOGGER.info(".................发布事件，map为:{}..........................", map);
        applicationContext.publishEvent(new HelloEvent("say hello"));
        applicationContext.publishEvent("just string");
        getBeanA().print();
        return map;
    }

    /**
     * TODO BeanA使用@Scope("prototype")注解，则applicationContext获取该bean时每次都会生成bean，不会缓存起来。必须调用getBean才能重新生成，依赖注入只进行一次。
     * @return
     */
    private BeanA getBeanA() {
        return applicationContext.getBean("beanA", BeanA.class);
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
    @RequestMapping(value = "/helloPost",method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> helloPost(@RequestParam String hello) {
        LOGGER.info("入参为：{}", hello);
        Map<String, String> dataMap = new HashMap(2);
        dataMap.put("hello", hello);
        return dataMap;
    }

    /**
     * http://localhost:8081/practice/helloUser
     * @return
     */
    @RequestMapping(value = "/helloUser")
    @ResponseBody
    public User helloUser(String name) {
        return userService.query(name);
    }
}
