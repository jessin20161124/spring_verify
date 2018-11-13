package com.jessin.practice.annotation;

import java.lang.annotation.*;

/**
 * @author zexin.guo
 * @create 2018-11-12 下午9:09
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
// 加了这个注解，表示可以在子类中继承，注意是子类
@Inherited
public @interface HeritedAnnotation {
    String value() default "";
}
