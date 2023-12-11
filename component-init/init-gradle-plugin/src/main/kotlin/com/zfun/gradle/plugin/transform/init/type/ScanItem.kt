package com.zfun.gradle.plugin.transform.init.type

import com.zfun.gradle.plugin.transform.init.Constants

class ScanItem(val componentName: String) {
    val classList:ArrayList<String> = ArrayList()
    var markProcessed = false
    init {
        markProcessed = false
        classList.clear()
    }

    fun getInterfaceName(): String {
        return Constants.INIT_COMPONENT_PACAGE + "/" + componentName
    }

}