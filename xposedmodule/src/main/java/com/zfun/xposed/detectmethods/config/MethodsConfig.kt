package com.zfun.xposed.detectmethods.config

import com.zfun.xposed.GlobalInit
import com.zfun.xposed.MyHelper
import com.zfun.xposed.detectmethods.type.MethodInfo
import de.robv.android.xposed.callbacks.XC_LoadPackage
import timber.log.Timber
import java.util.*

object MethodsConfig {

    fun getDefaultMethods(lpparam: XC_LoadPackage.LoadPackageParam): Set<MethodInfo> {
        val myApkPath = getModuleApkPath(lpparam)
        Timber.tag(GlobalInit.apkPackageName).d("module-apk path：$myApkPath")
        myApkPath?:return Collections.emptySet()
        return getMethodsFromAsset(myApkPath,"detectmethods/detect_methods.json")
    }

    fun getMethodsFromAsset(apkPath:String,assetFileName:String): Set<MethodInfo> {
        val textByte = MyHelper.readAssetsFile(apkPath, assetFileName)
        textByte?.apply {
            val jsonStr = String(this)
            Timber.tag(GlobalInit.apkPackageName).d("%s 读取到的检测方法：",apkPath)
            Timber.tag(GlobalInit.apkPackageName).d(jsonStr)
            return MethodInfoParse.parseMethodInfo(jsonStr)
        }
        return Collections.emptySet()
    }

    //data/user/0/io.va.exposed/virtual/data/app/com.zfun.xposedmodule/base.apk
    //data/user/0/io.va.exposed/virtual/data/app/cn.kuwo.tingshu.lite/base.apk
    private fun getModuleApkPath(lpparam: XC_LoadPackage.LoadPackageParam):String?{
        val hostApkPath = lpparam.appInfo.sourceDir
        val modulePackage = "com.zfun.xposedmodule"
        val lastIndex = hostApkPath.lastIndexOf('/')
        val lastSecIndex = hostApkPath.lastIndexOf('/',lastIndex-1)
        if(-1 == lastSecIndex){
            return null
        }
        return hostApkPath.substring(0,lastSecIndex) +"/"+ modulePackage + "/base.apk"
    }}