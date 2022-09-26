package com.zfun.xposed.detectmethods.service

import androidx.collection.ArraySet
import com.zfun.xposed.GlobalInit
import com.zfun.xposed.detectmethods.methodhookcallback.PrintStackTrackCallback
import com.zfun.xposed.detectmethods.type.MethodInfo
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import timber.log.Timber
import java.util.Collections
import java.util.concurrent.CopyOnWriteArraySet

object MethodHookMgr {
    interface ConfigUpdateListener {
        fun update()
    }//

    private val methodsSet = Collections.synchronizedSet(ArraySet<MethodInfo>())
    private val listeners = CopyOnWriteArraySet<ConfigUpdateListener>()

    @Synchronized
    fun addMethods(lpparam: XC_LoadPackage.LoadPackageParam, methodInfos: List<MethodInfo>) {
        methodsSet.addAll(methodInfos)
        beginHook(lpparam, *methodInfos.toTypedArray())
        notifyUpdate()
    }

    @Synchronized
    fun removeMethods(methodInfo: MethodInfo) {
        val removeOk = methodsSet.remove(methodInfo)
        if (removeOk) {
            unHook(methodInfo)
        }
        notifyUpdate()
    }

    @Synchronized
    fun refreshMethods(lpparam: XC_LoadPackage.LoadPackageParam, methodInfos: Set<MethodInfo>) {
        unHook(*methodsSet.toTypedArray())
        methodsSet.clear()
        methodsSet.addAll(methodInfos)
        beginHook(lpparam, *methodInfos.toTypedArray())
        notifyUpdate()
    }

    fun getAllMethods(): List<MethodInfo> {
        return ArrayList(methodsSet)
    }

    fun addUpdateListener(listener: ConfigUpdateListener) {
        listeners.add(listener)
    }

    fun removeUpdateListener(listener: ConfigUpdateListener) {
        listeners.remove(listener)
    }

    private fun notifyUpdate() {
        for (element in listeners) {
            element.update()
        }
    }

    private fun beginHook(lpparam: XC_LoadPackage.LoadPackageParam, vararg methodInfos: MethodInfo) {
        for (element in methodInfos) {
            if (!element.check()) {
                continue
            }
            val className = element.className
            val methodName = element.methodName
            val parameterTypesAndCallback = element.getArgTypes(lpparam,PrintStackTrackCallback())
            Timber.tag(GlobalInit.apkPackageName).d("beginHook ================================")
            Timber.tag(GlobalInit.apkPackageName).d("className：$className")
            Timber.tag(GlobalInit.apkPackageName).d("methodName：$methodName")
//            val parameterTypesArray = arrayOfNulls<Any>(parameterTypesAndCallback.size)
            for (logElement in parameterTypesAndCallback){
                Timber.tag(GlobalInit.apkPackageName).d("parameterType：$logElement")
            }
            val hookHandle = XposedHelpers.findAndHookMethod(
                    className,
                    lpparam.classLoader,
                    methodName,
                    *parameterTypesAndCallback.toTypedArray())
            element.unHook = hookHandle
            Timber.tag(GlobalInit.apkPackageName).d("end=======================================")
        }
    }

    private fun unHook(vararg methodInfos: MethodInfo) {
        for (element in methodInfos) {
            element.unHook?.apply {
                this.unhook()
            }
        }
    }
}