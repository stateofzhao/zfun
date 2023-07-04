package com.zfun.register;

//字节码扫描根据此接口来添加要处理的类
public interface IRegisterProvider {
    Class<?> key();
    default String fallbackKey(){
        return "";
    }
    Class<?> value();
}
