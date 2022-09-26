package com.zfun.xposed.detectmethods.methodhookcallback

import com.zfun.xposed.GlobalInit
import de.robv.android.xposed.XC_MethodHook
import timber.log.Timber
import java.io.PrintWriter
import java.io.StringWriter

class PrintStackTrackCallback() : XC_MethodHook() {
    override fun beforeHookedMethod(param: MethodHookParam?) {
        super.beforeHookedMethod(param)
    }

    override fun afterHookedMethod(param: MethodHookParam?) {
        super.afterHookedMethod(param)
        val stackStr = throwable2Str(Throwable())
        Timber.tag(GlobalInit.apkPackageName).e(stackStr)
    }


    private fun throwable2Str(tr: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        return sw.toString();
    }
}