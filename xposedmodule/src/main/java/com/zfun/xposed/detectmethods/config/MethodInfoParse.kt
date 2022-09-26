package com.zfun.xposed.detectmethods.config

import com.zfun.xposed.detectmethods.type.MethodInfo
import org.json.JSONArray
import java.util.*
import kotlin.collections.HashSet

object MethodInfoParse {

    fun parseMethodInfo(jsonStr: String): Set<MethodInfo> {
        if (jsonStr.isEmpty()) {
            return Collections.emptySet()
        }

        val jsonArray = JSONArray(jsonStr)
        val jsonArraySize = jsonArray.length()
        val resultList = HashSet<MethodInfo>(jsonArraySize)

        for (pos in 0 until jsonArraySize) {
            val jsonObject = jsonArray.getJSONObject(pos)
            val className = jsonObject.optString("className")
            val methodName = jsonObject.optString("methodName")
            val argsJsonArray = jsonObject.optJSONArray("args")
            val args = argsJsonArray?.let {
                val argsArray = arrayOfNulls<String>(it.length())
                for (argsPos in 0 until it.length()) {
                    argsArray[argsPos] = it.getString(argsPos)
                }
                argsArray
            }
            val methodInfo = args?.let { MethodInfo(className, methodName, *it) }
            methodInfo?.apply {
                resultList.add(methodInfo)
            }
        }
        return resultList
    }
}