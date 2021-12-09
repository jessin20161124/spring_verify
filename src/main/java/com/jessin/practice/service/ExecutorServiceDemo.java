package com.jessin.practice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zexin.guo
 * @create 2018-05-22 下午3:09
 **/
public class ExecutorServiceDemo {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceDemo.class);

    private static final ThreadPoolExecutor executorService =
            new ThreadPoolExecutor(12, 24,
                    1, TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>(120),
                    new CustomThreadFactory());

    public static class CustomThreadFactory implements ThreadFactory, Thread.UncaughtExceptionHandler {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "my_executor_" + threadNumber.getAndIncrement());
            t.setUncaughtExceptionHandler(this);
            return t;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            logger.error("Uncaught Exception in thread " + thread.getName(), throwable);
        }
    }
    public static void main(String[] args) throws Exception {
        for (int i = 1; i <= 3; i++) {
            runForOneSecond();
        }
        executorService.shutdown();
    }

    public static void runForOneSecond() {
        for (int i = 1; i <= 100; i++) {
            logger.info("提交第{}个任务", i + 1);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    logger.info("hello world start");
                    try {
                        Thread.sleep(250);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    throw new IllegalArgumentException("非法参数");
                    //logger.info("hello world end，存活线程数目 : {}", executorService.getActiveCount());
                }
            });
        }
    }
}
