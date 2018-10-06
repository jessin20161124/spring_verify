package com.jessin.practice.service.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author zexin.guo
 * @create 2018-09-26 下午5:41
 **/
@Component
@Aspect
@Slf4j
public class LogAop {
    @Pointcut("execution( * com.jessin.practice.service.HelloService.hello(..))")
    private void selfDefinePoincut() {
    }

    @Before("selfDefinePoincut()")
    public void beforeLogin(JoinPoint joinPoint) {
        log.info("before hello world，连接点信息为：{}", joinPoint);
    }

    @After("selfDefinePoincut()")
    public void afterLogin(JoinPoint joinPoint) {
        log.info("after hello world，连接点信息为：{}", joinPoint);
    }

    @Around("selfDefinePoincut()")
    public void aroundLogin(ProceedingJoinPoint proceedingJoinPoint) {
        log.info("around before1，连接点信息为：{}", proceedingJoinPoint);
        try {
            proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        log.info("around after1，连接点信息为：{}", proceedingJoinPoint);
    }
}
