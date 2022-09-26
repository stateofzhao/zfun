package com.zfun.xposed.detectmethods

import android.os.Process
import com.zfun.xposed.GlobalInit
import com.zfun.xposed.detectmethods.config.MethodsConfig
import com.zfun.xposed.detectmethods.service.MethodHookMgr
import com.zfun.xposed.detectmethods.type.MethodInfo
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import timber.log.Timber

class DetectMethodsEntry : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        //init
        lpparam ?: return
        GlobalInit.apkPackageName = lpparam.appInfo.packageName
        GlobalInit.init()
        //
        Timber.tag(GlobalInit.apkPackageName).i("*******DetectMethodsEntry启动*******")
        Timber.tag(GlobalInit.apkPackageName).i("%s  %d",Thread.currentThread().name,Process.myPid())
        Timber.tag(GlobalInit.apkPackageName).i("所属包名：%s", lpparam.packageName)
        Timber.tag(GlobalInit.apkPackageName).i("进程名：%s",lpparam.processName)
        Timber.tag(GlobalInit.apkPackageName).i("**************************************")
        //
        //进行方法调用检测
        val assetMethods = MethodsConfig.getDefaultMethods(lpparam)//内置方法
        val apkSelfAssetMethods = MethodsConfig.getMethodsFromAsset(lpparam.appInfo.sourceDir,"detect_methods.json")//要检测的apk的内置规则
        val allMethods = HashSet<MethodInfo>()
        if (assetMethods.isNotEmpty()){
            allMethods.addAll(assetMethods)
        }
        if (apkSelfAssetMethods.isNotEmpty()){
            allMethods.addAll(apkSelfAssetMethods)
        }

        MethodHookMgr.refreshMethods(lpparam,allMethods)
    }


}