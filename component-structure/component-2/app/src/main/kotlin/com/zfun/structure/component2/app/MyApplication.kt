package com.zfun.structure.component2.app

import android.app.Application
import android.util.Log
import com.zfun.initapi.InitLifecycle
import com.zfun.initapi.InitMgr

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        InitMgr.init(InitLifecycle.APP_ONCREATE,null)
    }
}