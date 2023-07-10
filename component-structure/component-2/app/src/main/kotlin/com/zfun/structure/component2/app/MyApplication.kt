package com.zfun.structure.component2.app

import android.app.Application
import com.zfun.initapi.CommInitLifecycle
import com.zfun.initapi.InitMgr

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        InitMgr.init(CommInitLifecycle.APP_ONCREATE,null)
    }
}