package com.zfun.component1.module1

import androidx.fragment.app.Fragment
import com.zfun.component1.bridge.protocol.module1.IModule1
import com.zfun.component1.bridge.protocol.module1.Module1Data1

class Module1Protocol:IModule1 {

    override fun getMainFragment(): Fragment {
        TODO("Not yet implemented")
    }

    override fun getData(): Module1Data1 {
        TODO("Not yet implemented")
    }
}