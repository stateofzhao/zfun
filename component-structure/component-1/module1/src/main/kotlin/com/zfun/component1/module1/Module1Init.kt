package com.zfun.component1.module1

import android.util.Log
import com.zfun.annatation.init.AutoInit
import com.zfun.initapi.IInit
import com.zfun.initapi.IInitLifecycle

@AutoInit(name = "Module1Init", dependsOn = "Module2Init")
class Module1Init: IInit {
    override fun init(lifetime: IInitLifecycle, callback: IInit.Callback?) {
        Log.e("init","Module1#Init : ${lifetime.lifecycleName()}")
        callback?.end()
    }
}