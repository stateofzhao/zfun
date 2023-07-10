package com.zfun.structure.component1.app

import android.app.Application
import com.zfun.initapi.CommInitLifecycle
import com.zfun.initapi.InitMgr
import com.zfun.register.RegisterCenter

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        InitMgr.init(CommInitLifecycle.APP_ONCREATE,null)

        RegisterCenter.printAllRegister()
    }
}