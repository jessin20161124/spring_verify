package com.jessin;

import org.junit.Test;

/**
 * @author zexin.guo
 * @create 2019-03-03 下午11:08
 **/
public class ThreadTest {
    @Test
    public void test() {
        Runnable runnable = () -> {
                System.out.println("hello world");
                throw new IllegalArgumentException("ha ha");
        };
         Thread th = new Thread(runnable);
         th.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
             @Override
             public void uncaughtException(Thread t, Throwable e) {
                 System.out.println("出异常了");
             }
         });
         th.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
