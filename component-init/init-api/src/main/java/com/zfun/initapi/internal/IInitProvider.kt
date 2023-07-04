package com.zfun.initapi.internal

interface IInitProvider {
    //被 @InitInAndroid 注解的类的实例
    fun get(): Any

    fun name(): String {
        return ""
    }

    fun dependsOn(): Array<String> {
        return arrayOf()
    }
}