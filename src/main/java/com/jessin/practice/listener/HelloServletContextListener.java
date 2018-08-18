package com.jessin.practice.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by zexin.guo on 17-8-8.
 */
public class HelloServletContextListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("start to init");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("end ...");
    }
}
