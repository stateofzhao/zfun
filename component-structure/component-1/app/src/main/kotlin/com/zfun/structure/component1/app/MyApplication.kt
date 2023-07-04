package com.zfun.structure.component1.app

import android.app.Application
import android.util.Log
import com.zfun.initapi.InitLifecycle
import com.zfun.initapi.InitMgr
import com.zfun.register.RegisterCenter

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        InitMgr.init(InitLifecycle.APP_ONCREATE,null)

        RegisterCenter.printAllRegister()
    }
}