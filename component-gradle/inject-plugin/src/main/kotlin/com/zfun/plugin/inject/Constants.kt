package com.zfun.plugin.inject

import com.android.utils.FileUtils
import org.gradle.api.Project
import java.io.File

object Constants {
    const val InputType_Class = "class" //仅处理class文件
    const val Scope_Project = "project" //整个项目
    const val Status_NOTCHANGED = "notChanged"
    const val Status_ADDED = "added"
    const val Status_CHANGED = "changed"
    const val Status_REMOVED = "removed"
    const val Status_NEW = "new" //重新编译

    const val BuildFileDirName = "zfun"
    const val BuildFileInject = "transform-inject"
    const val BuildFileInjectJar = "jar-opt"
    const val BuildFileInjectJarUn = "unzip"
    const val BuildFileInjectJarModified = "modified"


    fun getUnzipJarTempDir(project: Project, jarName: String): File {
        val rootDir = File(project.buildDir, BuildFileDirName + File.separator + BuildFileInjectJar)
        val result = File(rootDir, jarName + File.separator + BuildFileInjectJarUn)
        FileUtils.mkdirs(result)
        return result
    }

    fun getModifiedJarTempDir(project: Project, jarName: String):File {
        val rootDir = File(project.buildDir, BuildFileDirName + File.separator + BuildFileInjectJar)
        val result = File(rootDir, jarName + File.separator + BuildFileInjectJarModified)
        FileUtils.mkdirs(result)
        return result
    }

    //将此包下的所有类的第一个静态方法
    const val Inject_Package_Name = "com.zfun.processor.init"
    //注入到 class#method() 方法末尾
    const val Inject2_Class_Name = "InitMgr"
    const val Inject2_Class_Method_Name = "registerInitInAndroid"

}