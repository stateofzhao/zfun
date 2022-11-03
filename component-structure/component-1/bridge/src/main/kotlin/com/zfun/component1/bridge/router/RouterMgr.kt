package com.zfun.component1.bridge.router

import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

object RouterMgr {
    interface IRouter {
        fun optRoute(scheme:String,result: (Boolean) -> Unit)
    }//

    interface IFailureSty {
        fun routerConsumed(router: IRouter?, scheme: String, suc: Boolean)
    }//

    private val routerMap = ConcurrentHashMap<String, IRouter>()
    private val isRelease: AtomicBoolean = AtomicBoolean(false)
    private val mainScope = MainScope()

    fun register(scheme: String, router: IRouter) {
        if (isRelease.get()) return
        mainScope.launch {
            routerMap[scheme] = router
        }
    }

    fun unRegister(scheme: String) {
        mainScope.launch {
            routerMap.remove(scheme)
        }
    }

    fun route(scheme: String) {
        route(scheme, DefaultFailureSty())
    }

    fun route(scheme: String, failureSty: IFailureSty) {
        mainScope.launch {
            val router = routerMap[scheme]
            if (null == router) {
                failureSty.routerConsumed(null, scheme, false)
            } else {
                router.optRoute(scheme) {
                    failureSty.routerConsumed(router, scheme, it)
                }
            }
        }
    }

    fun release() {
        isRelease.set(true)
        routerMap.clear()
    }
}


