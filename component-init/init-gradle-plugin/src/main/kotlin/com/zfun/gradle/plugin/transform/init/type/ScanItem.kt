package com.zfun.gradle.plugin.transform.init.type

import com.zfun.gradle.plugin.transform.init.Constants

class ScanItem(val componentName: String) {
    val classList:ArrayList<String> = ArrayList()

    fun getInterfaceName(): String {
        return Constants.INIT_COMPONENT_PACAGE + "/" + componentName
    }

}