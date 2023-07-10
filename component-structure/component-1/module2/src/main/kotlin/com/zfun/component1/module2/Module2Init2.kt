package com.zfun.component1.module2

import android.util.Log
import com.zfun.annatation.init.AutoInit
import com.zfun.initapi.IInitLifecycle

@AutoInit
class Module2Init2 {
    fun init(lifetime: IInitLifecycle) {
        Log.e("init", "Module2#Init2 : ${lifetime.lifecycleName()}")
    }
}