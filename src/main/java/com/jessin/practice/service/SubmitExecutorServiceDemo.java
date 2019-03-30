package com.jessin.practice.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zexin.guo
 * @create 2019-03-02 下午4:27
 **/
@Slf4j
public class SubmitExecutorServiceDemo {

    public static <T> List<T> submit(ThreadPoolExecutor threadPool, List<? extends Callable<T>> taskList, long oneTaskWaitMillis) {
        List<Future<T>> futureList = Lists.newArrayListWithCapacity(taskList.size());
        taskList.stream().forEach(task ->
                {
                    Future<T> future = threadPool.submit(task);
                    futureList.add(future);
                }
        );
        int success = 0;

        System.out.println("活跃线程数：" + threadPool.getActiveCount());
        //轮询futureList
        List<T> resultList = Lists.newArrayListWithCapacity(taskList.size());
        for (Future<T> future : futureList) {
            try {
                System.out.println(JSON.toJSONString(future));
                T returnValue = Uninterruptibles.getUninterruptibly(future, oneTaskWaitMillis, TimeUnit.MILLISECONDS);
                System.out.println("返回值：" + returnValue);
                success++;
            } catch (ExecutionException e) {
                log.error(e.getMessage(), e);
            } catch (TimeoutException e) {
                log.error("超时了：", e);
            }
        }
        System.out.println("任务成功执行个数：" + success);
        return resultList;
    }

    public static void main(String[] args) {
        RejectedExecutionHandler logAndMonitor = (runnable, executionHandler) -> {
        };
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 40, 1, TimeUnit.MINUTES,
                new LinkedBlockingQueue(10000), new ThreadFactory() {
            AtomicInteger atomicInteger = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread =
                        new Thread(r, "my_executor_" + atomicInteger.incrementAndGet());
                thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        System.out.println("线程捕获到异常了：" + e);
                    }
                });
                return thread;
            }
        }, logAndMonitor);
        List<CallableTask> callableTasks = Lists.newArrayList();
        for (int i = 0; i < 1000; i++) {
            CallableTask callableTask = new CallableTask(i);
            callableTasks.add(callableTask);
        }

        submit(threadPool, callableTasks, 3000);
        // runnable则线程自己设置的异常处理器会生效，callable则会转化为ExecutionException
        // 在future.get()时抛出，如果不get则不会抛出，会被吞掉
        threadPool.execute(() -> {
            System.out.println("hello abc");
            throw new IllegalArgumentException("错误额");
        });

    }


}

class CallableTask implements Callable<Object> {

    private int id;

    public CallableTask(int id) {
        this.id = id;
    }
    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Object call() throws Exception {
        try {
            System.out.println("任务" + id + "开始执行");
            Thread.sleep(3000);
            System.out.println("任务" + id + "执行结束");

        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("出错了");
        //return null;
    }
}
