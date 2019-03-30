package com.jessin.practice.service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

/**
 * @author zexin.guo
 * @create 2019-03-03 下午10:30
 **/
public class ExecutorServiceDemo3 {

    static RejectedExecutionHandler logAndMonitor = (runnable, executionHandler) -> {};
    static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 40, 1, TimeUnit.MINUTES,
            new LinkedBlockingQueue(10000), new ThreadFactory() {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "my_executor_" + atomicInteger.incrementAndGet());
        }
    }, logAndMonitor);

    public static TripPlan selectBestTripPlanContinue(List<ServiceSupplier> serviceList) {
          return serviceList.stream()
                .map(svc -> CompletableFuture.supplyAsync(svc::createPlan, threadPool))

                .min(Comparator.comparing(cf -> cf.join().getPrice()))
                .get()
                .join();
    }
    public static TripPlan selectBestTripPlan(List<ServiceSupplier> serviceList) {
        List<CompletableFuture<TripPlan>> tripPlanFutures = serviceList.stream()
                .map(svc -> CompletableFuture.supplyAsync(svc::createPlan, threadPool))
                .collect(toList());

        return tripPlanFutures.stream()
                .min(Comparator.comparing(cf -> cf.join().getPrice()))
                .get()
                .join();
    }
    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        ServiceSupplier serviceSupplier = new ServiceSupplierImpl();
        List<ServiceSupplier> serviceSupplierList = Lists.newArrayList(serviceSupplier,
                serviceSupplier, serviceSupplier, serviceSupplier, serviceSupplier, serviceSupplier
        ,serviceSupplier,serviceSupplier,serviceSupplier,serviceSupplier);
        selectBestTripPlan(serviceSupplierList);
        System.out.println("耗时：" + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
    }
}
interface ServiceSupplier {
    TripPlan createPlan();
    String getAlliance();       // 稍后使用
}
class ServiceSupplierImpl implements ServiceSupplier {
    Random r = new Random(1);
    @Override
    public TripPlan createPlan() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (r.nextBoolean()) {
            throw new IllegalArgumentException("ha ha ha");
        }
        return new TripPlan();
    }

    @Override
    public String getAlliance() {
        return null;
    }
}
class TripPlan {
    public int getPrice() {
        return 100;
    }
}