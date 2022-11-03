package com.zfun.component1.bridge.protocol

import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

object ProtocolRegister {
    //让此属性可以被public的内联函数访问到，并且还希望外部不要访问它，目前kotlin只支持 internal+ @PublishedApi，
    // 不支持 private + @PublishedApi.
    @PublishedApi
    internal val protocolMap: MutableMap<String, Any> = Collections.synchronizedMap(HashMap<String, Any>())
    @PublishedApi
    internal val isRelease: AtomicBoolean = AtomicBoolean(false)

    fun registerProtocol(name: String, protocol: Any) {
        if (isRelease.get()) {
            return
        }
        protocolMap[name] = protocol
    }

    //reified关键字可以让范型保留类型.
    //reified必须配合inline使用.
    inline fun <reified T> getProtocol(name: String): T? {
        if (isRelease.get()) {
            return null
        }
        val protocol = protocolMap[name]
        return if (protocol is T) {
            protocol
        } else {
            null
        }
    }

    fun release() {
        isRelease.set(true)
        protocolMap.clear()
    }
}