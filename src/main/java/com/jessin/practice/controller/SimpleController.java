package com.jessin.practice.controller;

import com.jessin.practice.bean.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zexin.guo
 * @create 2018-05-05 下午5:15
 **/
@Component
public class SimpleController extends AbstractController {
    /**
     * Template method. Subclasses must implement this.
     * The contract is the same as for {@code handleRequest}.
     *
     * @param request
     * @param response
     * @see #handleRequest
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        MappingJackson2JsonView view = new MappingJackson2JsonView();
        // 当只有一个model需要渲染时，不要key，只要value
        view.setExtractValueFromSingleKeyModel(true);
        ModelAndView mv = new ModelAndView(view);
        List<User> list = new ArrayList<User>(0);
        User user1 = new User();
        user1.setId(1);
        user1.setBirthday(new Date());
        user1.setName("tony");
        list.add(user1);

        User user2 = new User();
        user2.setId(2);
        user2.setBirthday(new Date());
        user2.setName("amy");
        list.add(user2);
        mv.addObject(list);
        return mv;
    }
}
