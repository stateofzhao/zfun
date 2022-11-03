package com.zfun.component1.bridge.protocol.module3

import androidx.fragment.app.Fragment

interface IModule3 {
    fun getMainFragment(): Fragment
    fun getData(): Module3Data1
}