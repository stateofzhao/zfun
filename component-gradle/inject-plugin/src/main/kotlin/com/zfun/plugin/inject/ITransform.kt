package com.zfun.plugin.inject

import org.gradle.api.Project
import java.io.File

interface ITransform {
    fun name(): String {
        return "ITransformOpt"
    }

    /**
     * 是否支持增量编译
     * @return true支持，false不支持
     * */
    fun isIncremental(): Boolean {
        return true
    }

    /**
     * 处理的文件类型
     *
     * @return {@link Constants#InputType_Class}等
     * */
    fun getInputTypes(): String {
        return Constants.InputType_Class
    }

    /**
     * 扫描范围
     *
     * @return {@link Constants#Scope_Project}等
     * */
    fun getScopes(): String {
        return Constants.Scope_Project
    }

    /**
     * @param project Gradle的Project类，一般为打包的app工程，因为目前Transform只能注册给 Android Gradle Plugin 的 AppExtension 扩展.
     * @param inputFile 传递进来的 getInputTypes() 文件
     * @param outputFile 将修改后的 getInputTypes() 文件存放到此文件中，此文件如果不存在需要创建
     * */
    fun transform(project: Project, inputFile: File, outputFile: File): Boolean

    /**
     * 是否需要先扫描一边文件
     *
     * @return true需要；false不需要
     * */
    fun isNeedScanAllFile(): Boolean {
        return false
    }

    /**
     * 只有{@link #isNeedScanAllFile()}返回true时，才会会掉此方法
     * */
    fun scan(project: Project, inputFile: File) {

    }
}