package com.jessin.practice.servlet;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 3.x以上servlet支持注解方式
 * @author zexin.guo
 * @create 2018-10-21 下午4:31
 **/
@WebServlet(urlPatterns = "/hello")
@Slf4j
public class HelloWorldServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("调用功能servlet");
            response.setHeader("content-type", "text/html; charset=utf-8");
            response.getOutputStream().write("老司机来也".getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
