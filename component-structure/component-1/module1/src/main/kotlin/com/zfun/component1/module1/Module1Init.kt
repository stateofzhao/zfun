package com.zfun.component1.module1

import android.util.Log
import com.zfun.annatation.init.AutoInit
import com.zfun.initapi.IInit
import com.zfun.initapi.InitLifecycle

@AutoInit(name = "Module1Init", dependsOn = "Module2Init")
class Module1Init: IInit {
    override fun init(lifetime: InitLifecycle, callback: IInit.Callback?) {
        Log.e("init","Module1#Init : ${lifetime.name}")
        callback?.end()
    }
}