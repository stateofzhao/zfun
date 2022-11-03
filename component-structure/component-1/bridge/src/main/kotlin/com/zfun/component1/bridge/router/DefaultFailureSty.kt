package com.zfun.component1.bridge.router

import android.net.Uri

//回退策略
class DefaultFailureSty : RouterMgr.IFailureSty {
    override fun routerConsumed(router: RouterMgr.IRouter?, scheme: String, suc: Boolean) {
        if (suc){
            return
        }
        //https://github.com/stateofzhao/zfun
        val tryScheme = scheme.substringBeforeLast("/")
        //tryScheme == https://github.com/stateofzhao
        val uri = Uri.parse(tryScheme)
        val path = uri.path
        path?:return
        RouterMgr.route(tryScheme,this)
    }

}