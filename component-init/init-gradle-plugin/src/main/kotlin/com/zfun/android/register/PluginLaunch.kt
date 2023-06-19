package com.zfun.android.register

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.zfun.android.register.utils.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginLaunch : Plugin<Project> {
    companion object{
        const val isDebug = true
    }

    override fun apply(project: Project) {
        Logger.init(project)
        val haveAppPlugin = project.plugins.hasPlugin(AppPlugin::class.java)
        if (haveAppPlugin) {
            Logger.i("start init generatorCode")
            val appExtension = project.extensions.findByType(AppExtension::class.java)
            val registerTransform = RegisterTransform()
            appExtension?.registerTransform(registerTransform)
        }
    }
}