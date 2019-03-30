package com.jessin.practice.service;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zexin.guo
 * @create 2019-03-30 下午8:35
 **/
public class RateLimiterTest {
    /**
     * TODO QPS 为0.1，每秒有多少个可以过去，线程安全
     */
    RateLimiter rateLimiter = RateLimiter.create(0.1);
    ExecutorService executorService = new ThreadPoolExecutor(10, 10,
            0, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>());

    public void execute() {
        Runnable runnable = () -> {
            if (rateLimiter.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                System.out.println(System.currentTimeMillis() + " 100ms内可以拿到令牌，执行中");
            } else {
                //System.out.println("100ms内无法拿到令牌，直接放弃了");
            }
        };

        long currentTimeMillis = System.currentTimeMillis();
        boolean into = false;
        while(true) {
            executorService.execute(runnable);
            if (!into && System.currentTimeMillis() - currentTimeMillis > 10000) {
                rateLimiter.setRate(1);
                System.out.println("增大频率");
                into = true;
            }
        }
       // executorService.shutdown();
    }

    public static void main(String[] args) {
        RateLimiterTest rateLimiterTest = new RateLimiterTest();
        rateLimiterTest.execute();
    }
}
