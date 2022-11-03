package com.zfun.component1.module2

import com.zfun.annotation.InitInAndroid
import com.zfun.processor.init.IInit
import com.zfun.processor.init.InitLifetime

@InitInAndroid
class Module2Init : IInit {
    override fun init(lifetime: InitLifetime?) {
        println("Module2Init : ${lifetime?.name}")
    }
}