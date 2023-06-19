package com.zfun.android.register.type

import com.zfun.android.register.Constants

class ScanItem(val componentName: String) {
    val classList:ArrayList<String> = ArrayList()

    fun getInterfaceName(): String {
        return Constants.INIT_COMPONENT_PACAGE + "/" + componentName
    }

}