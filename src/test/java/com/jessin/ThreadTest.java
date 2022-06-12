package com.jessin;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author zexin.guo
 * @create 2019-03-03 下午11:08
 **/
@Slf4j
public class ThreadTest {

    @Test
    public void test() {
        Runnable runnable = () -> {
            log.info("hello world");
            throw new IllegalArgumentException("ha ha");
        };
        Thread th = new Thread(runnable);
        th.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                log.error("{}, 出异常了", t.getName(), e);
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
