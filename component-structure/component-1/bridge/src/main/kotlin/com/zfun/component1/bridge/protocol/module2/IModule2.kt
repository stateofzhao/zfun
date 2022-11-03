package com.zfun.component1.bridge.protocol.module2

import androidx.fragment.app.Fragment

interface IModule2 {
    fun getMainFragment(): Fragment
    fun getData(): Module2Data1
}