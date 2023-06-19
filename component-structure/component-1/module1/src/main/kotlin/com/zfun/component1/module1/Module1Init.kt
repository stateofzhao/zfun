package com.zfun.component1.module1

import android.util.Log
import com.zfun.annotation.InitInAndroid
import com.zfun.initapi.IInit
import com.zfun.initapi.InitLifecycle

@InitInAndroid
class Module1Init: IInit {
    override fun init(lifetime: InitLifecycle) {
        Log.e("init","Module1#Init : ${lifetime.name}")
    }
}