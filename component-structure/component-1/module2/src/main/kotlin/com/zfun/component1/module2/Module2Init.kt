package com.zfun.component1.module2

import android.util.Log
import com.zfun.annatation.init.AutoInit
import com.zfun.initapi.IInit
import com.zfun.initapi.IInit.Callback
import com.zfun.initapi.InitLifecycle

@AutoInit(name = "Module2Init", dependsOn = "aaa")
class Module2Init : IInit {
    override fun init(lifetime: InitLifecycle,callback: Callback?) {
        Log.e("init","Module2#Init : ${lifetime.name}")
        callback?.end()
    }
}