package com.zfun.component1.app

import android.app.Application
import com.zfun.processor.init.InitMgr

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        InitMgr.getInstance().registerInitInAndroid()
    }
}