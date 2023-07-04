package com.zfun.gradle.plugin.transform.utils

import com.zfun.gradle.plugin.transform.AbsPluginLaunch
import org.gradle.api.Project
import org.gradle.api.logging.Logger

object Logger {
    lateinit var logger: Logger

    fun init(project: Project) {
        logger = project.logger
    }

    fun i(info: String) {
//        logger.info("${PluginLaunch.adapter.pluginName()}::Register >>> $info")
        println("${AbsPluginLaunch.adapter.pluginName()}::Register >>> $info")
    }

    fun w(warn: String) {
//        logger.warn("${PluginLaunch.adapter.pluginName()}::Register >>> $warn")
        println("${AbsPluginLaunch.adapter.pluginName()}::Register >>> $warn")
    }

    fun e(error: String) {
//        logger.error("${PluginLaunch.adapter.pluginName()}::Register >>> $error")
        println("${AbsPluginLaunch.adapter.pluginName()}::Register >>> $error")
    }
}