package com.zfun.component1.module1

import com.zfun.annotation.InitInAndroid
import com.zfun.processor.init.IInit
import com.zfun.processor.init.InitLifetime

@InitInAndroid
class Module1Init: IInit {
    override fun init(lifetime: InitLifetime?) {
        lifetime?:return
        if (InitLifetime.APP_ONCREATE == lifetime){
            println("app_onCreate方法调用")
        }
    }
}