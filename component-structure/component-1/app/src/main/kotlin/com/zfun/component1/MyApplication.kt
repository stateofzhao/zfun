package com.zfun.component1

import android.app.Application
import android.util.Log
import com.zfun.initapi.InitLifecycle
import com.zfun.initapi.InitMgr

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        InitMgr.init(InitLifecycle.APP_ONCREATE)
        Log.e("MyApplication","初始化类个数："+InitMgr.iInitList.size)
    }
}