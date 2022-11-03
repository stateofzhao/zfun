package com.zfun.component1.bridge.protocol.module1

import androidx.fragment.app.Fragment

interface IModule1 {
    fun getMainFragment(): Fragment
    fun getData(): Module1Data1
}