package com.jessin;

import com.jessin.practice.service.HelloService;
import com.jessin.practice.service.advice.HelloBeforeAdvice;
import com.jessin.practice.service.advice.WorldBeforeAdvice;
import com.jessin.practice.service.impl.HelloServiceImpl;
import java.util.Queue;
import org.aopalliance.aop.Advice;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

import java.lang.reflect.Method;

/**
 * @author zexin.guo
 * @create 2018-09-29 上午9:51
 **/
public class ProxyFactoryTest {
    private Advice helloBeforeAdvice = new HelloBeforeAdvice();

    private Advice worldBeforeAdvice = new WorldBeforeAdvice();

    /**
     * TODO 如果代理的对象不是接口呢？是否需要改为cglib代理？
     */
    private HelloService target = new HelloServiceImpl();

    /**
     * 所有方法都被代理？
     */
    @Test
    public void test1() {
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvice(helloBeforeAdvice);
        Object proxyBean = proxyFactory.getProxy();
        ((HelloService)proxyBean).hello();
        // TODO 能够动态添加advisor
        ((Advised)proxyBean).addAdvice(worldBeforeAdvice);
        ((HelloService)proxyBean).hello();
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
        defaultPointcutAdvisor.setAdvice(helloBeforeAdvice);
        // TODO spring中target即为bean，可以自动创建代理，下面两个可变
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvisor(defaultPointcutAdvisor);

        HelloService helloService = (HelloService)proxyFactory.getProxy();
        helloService.hello();

    }
}
