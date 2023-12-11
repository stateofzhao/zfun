package com.zfun.gradle.plugin.transform.init

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.google.gson.GsonBuilder
import com.zfun.gradle.plugin.transform.init.utils.Logger
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.FileWriter
import java.util.regex.Pattern

class PluginLaunch : Plugin<Project> {

    companion object{
        const val buildResultLogDirName = "zfunInit"
        const val buildResultLogFileName = "output-zfun-transform.json"
    }

    //无论项目编译换成怎么调整，只要apply了此plugin，执行时一定会走这个方法，但是transform不一定执行
    override fun apply(project: Project) {
        Logger.init(project)
        val haveAppPlugin = project.plugins.hasPlugin(AppPlugin::class.java)
        if (haveAppPlugin) {
            Logger.i("start init generatorCode plugin")
            RegisterTransform.init()//重置
            val appExtension = project.extensions.findByType(AppExtension::class.java)
            val registerTransform = RegisterTransform{
                configSaveLog(project)
            }
            appExtension?.registerTransform(registerTransform)
            ///
            //打包完成输出字节码修改情况，便于确认包是否包含了指定的功能
            project.gradle.projectsEvaluated {
                configPrint(project)
            }
        }
    }

    private fun configSaveLog(project: Project){
        //更新本地的输出数据
        val gson = GsonBuilder().setPrettyPrinting().create()
        val resultJsonStr = gson.toJson(RegisterTransform.scanComponent)
        val buildDir = project.buildDir
        val desParentDir = File(buildDir,buildResultLogDirName)
        val androidEx = project.extensions.findByName("android") as? BaseAppModuleExtension ?:return
        androidEx.applicationVariants.all {
            val curVariant = getCurrentVariant(project)
            if (it.name.equals(curVariant, ignoreCase = true)){
                Logger.i("find current variant name : $curVariant")
                val desDir = File(desParentDir,it.name)
                FileUtils.forceMkdir(desDir)
                val desFile = File(desDir,buildResultLogFileName)
                if (desFile.exists()){
                    FileUtils.forceDelete(desFile)
                }
                val fileWriter = FileWriter(desFile,false)
                fileWriter.write(resultJsonStr)
                fileWriter.close()
            }
        }
    }

    private fun configPrint(project: Project){
        val androidEx = project.extensions.findByName("android") as? BaseAppModuleExtension ?:return
        androidEx.applicationVariants.forEach { applicationVariant ->
            applicationVariant.assembleProvider.get().doLast {
                Logger.i("assemble finish : start print zfun transform result")
                val buildDir = project.buildDir
                val srcParentDir = File(buildDir,buildResultLogDirName)
                val srcDir = File(srcParentDir,applicationVariant.name)
                val srcFile = File(srcDir,buildResultLogFileName)
                //
                val apkFilePath = applicationVariant.outputs.stream().findFirst().get().outputFile
                val apkParentDir = apkFilePath.parent
                val desFile = File(apkParentDir,buildResultLogFileName)
                if (desFile.exists()){
                    FileUtils.forceDelete(desFile)
                }
                FileUtils.copyFile(srcFile,desFile)
                Logger.i("assemble finish : zfun transform result == ${desFile.absolutePath}")
            }
        }
    }

    private fun getCurrentFlavor(project: Project):String{
        val gradle = project.gradle
        val tskReqStr = gradle.startParameter.taskRequests.toString()
        val pattern =  if( tskReqStr.contains( "assemble" ) ) // to run ./gradlew assembleRelease to build APK
             Pattern.compile("assemble(\\w+)(Release|Debug)")
        else if( tskReqStr.contains( "bundle" ) ) // to run ./gradlew bundleRelease to build .aab
            Pattern.compile("bundle(\\w+)(Release|Debug)")
        else
            Pattern.compile("generate(\\w+)(Release|Debug)")

        val matcher = pattern.matcher(tskReqStr)
        return if (matcher.find()) {
            val curFlavor = matcher.group(1)
            curFlavor
        }
        else {
            Logger.i("can not find current flavor!!!")
            ""
        }
    }

    private fun getCurrentVariant(project: Project):String{
        val gradle = project.gradle
        val tskReqStr = gradle.startParameter.taskRequests.toString()
        val pattern =  if( tskReqStr.contains( "assemble" ) ) // to run ./gradlew assembleRelease to build APK
            Pattern.compile("assemble(\\w+)")
        else if(tskReqStr.contains( "bundle" )) // to run ./gradlew bundleRelease to build .aab
            Pattern.compile("bundle(\\w+)")
        else
            Pattern.compile("generate(\\w+)")

        val matcher = pattern.matcher(tskReqStr)
        return if (matcher.find()) {
            val curVariant = matcher.group(1)
            curVariant
        }
        else {
            Logger.i("can not find current variant!!!")
            ""
        }
    }

}