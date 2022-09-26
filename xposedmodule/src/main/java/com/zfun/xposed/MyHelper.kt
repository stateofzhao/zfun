package com.zfun.xposed

import android.content.res.XModuleResources
import de.robv.android.xposed.XposedHelpers
import java.lang.Exception

object MyHelper {
    fun readAssetsFile(apkPath: String, assetFilePath: String): ByteArray? {
        return try {
            val res = XModuleResources.createInstance(apkPath, null)
            XposedHelpers.assetAsByteArray(res,assetFilePath)
        }catch (ignore:Exception){
            null
        }
    }
}