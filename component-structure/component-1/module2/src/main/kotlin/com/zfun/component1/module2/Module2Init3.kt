package com.zfun.component1.module2

import android.util.Log
import com.zfun.annatation.init.AutoInit
import com.zfun.initapi.IInit
import com.zfun.initapi.IInit.Callback
import com.zfun.initapi.IInitLifecycle

@AutoInit(name = "Module2Init3", dependsOn = "Module2Init,Module1Init")
class Module2Init3:IInit {
    override fun init(lifetime: IInitLifecycle?, callback: Callback?) {
        Log.e("init","Module2Init3#Init : ${lifetime?.lifecycleName()}")
        callback?.end()
    }
}