package com.zfun.component1.module1

import timber.log.Timber

class Module1Function {
    fun doSomething1(){
        Timber.tag("Function").d("component1-module1-doSomething1")
    }

    fun doSomething2(){
        Timber.tag("Function").d("component1-module1-doSomething2")
    }

    fun doSomething3(){
        Timber.tag("Function").d("component1-module1-doSomething3")
    }

    fun doSomething4(){
        Timber.tag("Function").d("component1-module1-doSomething4")
    }

    fun getData():String{
        return "component1-module1-function"
    }
}