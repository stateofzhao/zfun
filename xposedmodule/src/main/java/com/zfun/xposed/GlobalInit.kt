package com.zfun.xposed

import com.zfun.xposedmodule.BuildConfig
import timber.log.Timber

object GlobalInit {
    private var isInit = false
    lateinit var apkPackageName:String

    fun init() {
        if (isInit) {
            return
        }
        isInit = true

        initLog()
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}