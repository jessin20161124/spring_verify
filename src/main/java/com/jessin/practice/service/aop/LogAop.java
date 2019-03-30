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
    @Pointcut("execution( * com.jessin..*.*Dao.*(..)) || execution( * com.jessin..*.*Mapper.*(..))")
    private void daoPointCut() {

    }

    @Pointcut("execution( * com.jessin.practice..*.*Controller.*(..))")
    private void selfDefinePointcut() {
    }

    @Before("selfDefinePointcut()")
    public void beforeLogin(JoinPoint joinPoint) {
        log.info("before hello world，连接点信息为：{}", joinPoint);
    }

    @After("selfDefinePointcut()")
    public void afterLogin(JoinPoint joinPoint) {
        log.info("after hello world，连接点信息为：{}", joinPoint);
    }

    /**
     * aroundLogin的before先，after后
     * @param proceedingJoinPoint
     */
    @Around("selfDefinePointcut()")
    public Object aroundLogin(ProceedingJoinPoint proceedingJoinPoint) {
        log.info("around before1，连接点信息为：{}", proceedingJoinPoint);
        Object ret = null;
        try {
            ret = proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        log.info("around after1，连接点信息为：{}，返回结果：{}", proceedingJoinPoint, ret);
        return ret;
    }

    @Around("daoPointCut()")
    public Object daoAround(ProceedingJoinPoint proceedingJoinPoint) {
        log.info("daoAround before1，连接点信息为：{}", proceedingJoinPoint);
        Object ret = null;
        try {
            ret = proceedingJoinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        log.info("daoAround after1，连接点信息为：{}，返回结果：{}", proceedingJoinPoint, ret);
        return ret;
    }
}
