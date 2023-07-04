package com.zfun.annotation.register;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * key - value 方式注册：
 * key 通过方法 {@link #key()} 定义；
 * value 为被注解的类的Class；
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface AutoRegister {
    Class<?> key() default Object.class;

    //如果返回是empty，那么此值就是被注解的类全称
    String fallbackKey() default "";
}
