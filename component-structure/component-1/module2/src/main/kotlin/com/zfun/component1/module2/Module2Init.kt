package com.zfun.component1.module2

import android.util.Log
import com.zfun.annotation.InitInAndroid
import com.zfun.initapi.IInit
import com.zfun.initapi.InitLifecycle

@InitInAndroid
class Module2Init : IInit {
    override fun init(lifetime: InitLifecycle) {
        Log.e("init","Module2#Init1 : ${lifetime.name}")
    }
}