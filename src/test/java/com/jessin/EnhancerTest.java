package com.jessin;

import com.jessin.practice.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.cglib.proxy.*;

import java.lang.reflect.Method;

/**
 * CGLIB使用
 * @author zexin.guo
 * @create 2018-10-03 下午3:43
 **/
@Slf4j
public class EnhancerTest {

    @Test
    public void test1() {
        // 所有方法都会被拦截，如toString/hashCode，逻辑在这里
        Callback callback = new MethodInterceptor() {
            @Override
            public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                log.info("before invoke : {}", method);
                // 原来的方法调用，连接点
                Object result = methodProxy.invokeSuper(proxy, args);
                log.info("after invoke : {}", method);
                return result;
            }
        };

        EnhancerTest enhancerTest = getProxy(EnhancerTest.class, callback);
        enhancerTest.test();
        log.info("hashcode : {}", enhancerTest.hashCode());
        log.info("judge is HelloService : {}", enhancerTest instanceof HelloService);
    }

    private <T> T getProxy(Class<T> superClass, Callback callback) {
        // 代理实例的获取
        Enhancer enhancer = new Enhancer();
        // 设置需要创建子类的类，final/private方法不能被代理
        enhancer.setInterfaces(new Class[] {HelloService.class});
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
