package com.jessin;

import com.jessin.practice.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.cglib.proxy.*;

import java.lang.reflect.Method;

/**
 * https://www.cnblogs.com/lvbinbin2yujie/p/10284316.html
 * https://www.cxyzjd.com/article/z69183787/106878203
 * CGLIB使用
 * @author zexin.guo
 * @create 2018-10-03 下午3:43
 **/
@Slf4j
public class EnhancerTest {

    @Test
    public void test1() {
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "/Users/jessin/Documents/program/java/spring_verify/target/cglib");
        // 所有方法都会被拦截，如toString/hashCode，逻辑在这里
        Callback callback = new MethodInterceptor() {
            @Override
            public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                log.info("before invoke : {}", method);
                // 原来的方法调用，连接点
                // 如果调用invoke(proxy)，则会不断调用自己，直至栈溢出
                // @Configuration做法，内嵌会走增强
                // 报错：Object result = methodProxy.invokeSuper(new EnhancerTest(), args);
                Object result = methodProxy.invokeSuper(proxy, args);
                // AOP做法，内嵌不会走增强
                // Object result = methodProxy.invoke(target, args);
                log.info("after invoke : {}", method);
                return result;
            }
        };

        EnhancerTest enhancerTest = getProxy(EnhancerTest.class, callback);
        enhancerTest.test();
        log.info("hashcode : {}", enhancerTest.hashCode());
        log.info("judge is HelloService : {}", enhancerTest instanceof HelloService);
        // 报错，需要在intercept中处理，难道这里是标记接口？
      //  ((HelloService)enhancerTest).hello();
    }

    private <T> T getProxy(Class<T> superClass, Callback callback) {
        // 代理实例的获取
        Enhancer enhancer = new Enhancer();
        // 设置需要创建子类的类，final/private方法不能被代理，这个接口不管用？
        enhancer.setInterfaces(new Class[] {HelloService.class});
        // 需要创建的父类
        enhancer.setSuperclass(superClass);
        enhancer.setCallbackFilter(new CallbackFilter() {
            @Override
            public int accept(Method method) {
                // callback数组的下标
                return 1;
            }
        });
        enhancer.setCallbacks(new Callback[]{callback,callback});
        // EnhancerTest的子类
        return (T) enhancer.create();
    }

    public void test() {
        log.info("EnhancerTest test()");
    }
}
