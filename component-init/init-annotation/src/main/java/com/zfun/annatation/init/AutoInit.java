package com.zfun.annatation.init;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//RetentionPolicy.SOURCE 注解仅存在于源码中，在class字节码中无法获取
//RetentionPolicy.CLASS 默认保留策略，注解会在class字节码文件中存在，但运行时无法获得
//RetentionPolicy.RUNTIME 注解会存在class字节码文件中存在，在运行时可以通过反射获取到
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface AutoInit {
    String name() default "";

    /**
     * 如果依赖多个模块，以","分割（这里不分先后）
     *
     * @return 返回依赖的模块名
     * */
    String dependsOn() default "";
}
