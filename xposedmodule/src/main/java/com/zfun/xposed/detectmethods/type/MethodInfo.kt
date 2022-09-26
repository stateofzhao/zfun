package com.zfun.xposed.detectmethods.type

import com.zfun.xposed.util.ClassTypeHelper
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.RuntimeException
import java.util.Objects

class MethodInfo(val className: String, val methodName: String, private vararg val args: String?) {

    var unHook: XC_MethodHook.Unhook? = null
        set(value) {
            if (unHook != null) {
                throw RuntimeException("MethodInfo 只可被Hook一次，不可重复使用")
            } else {
                field = value
            }
        }

    fun check(): Boolean {
        return !isEmpty(className) && !isEmpty(methodName)
    }

    fun getArgTypes(lpparam: XC_LoadPackage.LoadPackageParam, vararg hookCallback: XC_MethodHook): List<*> {
        val resultList = ArrayList<Any>(args.size)
        for (element in args) {
            if (isEmpty(element)) {
                continue
            }
            resultList.add(ClassTypeHelper.parseType(element, lpparam.classLoader))
        }
        resultList.addAll(hookCallback)
        return resultList
    }

    private fun isEmpty(text: String?): Boolean {
        text ?: return true
        return text.isEmpty();
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        val otherMe = other as MethodInfo
        if (className == other.className && methodName == other.methodName) {
            if (args.size != otherMe.args.size) {
                return false
            }
            var equals = true
            for (pos in args.indices) {
                if (args[pos] != otherMe.args[pos]) {
                    equals = false
                    break
                }
            }
            return equals
        }
        return false
    }


    override fun hashCode(): Int {
        return Objects.hash(className, methodName)
    }
}