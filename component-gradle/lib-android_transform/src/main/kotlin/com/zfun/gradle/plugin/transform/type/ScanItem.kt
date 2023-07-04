package com.zfun.gradle.plugin.transform.type


/**
 * @param apiProviderClassSimpleName xxx-api下面 xxxProvider 接口简单名
 * @param apiProviderClassFullName xxx-api下面 xxxProvider 接口全称
 * */
data class ScanItem(val apiProviderClassSimpleName: String, val apiProviderClassFullName:String) {
    //xxxProvider 的所有实现类（这个是注解处理器生成的）
    val apiProviderImplClassList:ArrayList<String> = ArrayList()
}