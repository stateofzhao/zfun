package com.zfun.android.register.utils

import org.gradle.api.Project
import org.gradle.api.logging.Logger

object Logger {
    lateinit var logger: Logger

    fun init(project: Project) {
        logger = project.logger
    }

    fun i(info: String) {
        logger.info("Init::Register >>> $info")
    }

    fun w(warn: String) {
        logger.warn("Init::Register >>> $warn")
    }

    fun e(error: String) {
        logger.error("Init::Register >>> $error")
    }
}