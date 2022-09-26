package com.zfun.xposed.detectmethods

import com.zfun.xposed.GlobalInit
import de.robv.android.xposed.IXposedHookZygoteInit
import timber.log.Timber

class ZygotelInitEntry : IXposedHookZygoteInit {
    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam?) {
        //init
        startupParam ?: return
        GlobalInit.init()
        Timber.d("*******ZygotelInitEntry启动*******")
        Timber.d("apk所在位置：" + startupParam.modulePath)
        Timber.d("**************************************")
    }
}