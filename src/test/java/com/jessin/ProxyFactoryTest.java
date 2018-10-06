package com.jessin;

import com.jessin.practice.service.HelloService;
import com.jessin.practice.service.advice.HelloBeforeAdvice;
import com.jessin.practice.service.impl.HelloServiceImpl;
import org.aopalliance.aop.Advice;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

import java.lang.reflect.Method;

/**
 * @author zexin.guo
 * @create 2018-09-29 上午9:51
 **/
public class ProxyFactoryTest {
    private Advice advice = new HelloBeforeAdvice();
    private HelloService target = new HelloServiceImpl();

    /**
     * 所有方法都被代理？
     */
    @Test
    public void test1() {
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvice(advice);
        HelloService helloService = (HelloService)proxyFactory.getProxy();
        helloService.hello();
    }

    @Test
    public void test2() {
        DefaultPointcutAdvisor defaultPointcutAdvisor = new DefaultPointcutAdvisor();
        defaultPointcutAdvisor.setPointcut(new Pointcut() {
            @Override
            public ClassFilter getClassFilter() {
                return new ClassFilter() {
                    @Override
                    public boolean matches(Class<?> clazz) {
                        return true;
                    }
                };
            }

            @Override
            public MethodMatcher getMethodMatcher() {
                return new MethodMatcher() {
                    @Override
                    public boolean matches(Method method, Class<?> targetClass) {
                        return method.getName().equals("greetTo");
                    }

                    @Override
                    public boolean isRuntime() {
                        return false;
                    }

                    @Override
                    public boolean matches(Method method, Class<?> targetClass, Object... args) {
                        return false;
                    }
                };
            }
        });
        defaultPointcutAdvisor.setAdvice(advice);
        // TODO spring中target即为bean，可以自动创建代理，下面两个可变
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvisor(defaultPointcutAdvisor);

        HelloService helloService = (HelloService)proxyFactory.getProxy();
        helloService.hello();
    }
}
