package com.zfun.gradle.plugin.transform

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.zfun.gradle.plugin.transform.utils.Logger
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class AbsPluginLaunch : Plugin<Project> {
    companion object {
        var isDebug = true
        lateinit var adapter: IPluginLaunchAdapter
    }

    override fun apply(project: Project) {
        adapter = createAdapter()
        isDebug = adapter.isDebug()
        Logger.init(project)
        val haveAppPlugin = project.plugins.hasPlugin(AppPlugin::class.java)
        if (haveAppPlugin) {
            Logger.i("start ${adapter.pluginName()} generatorCode")
            val appExtension = project.extensions.findByType(AppExtension::class.java)
            val registerTransform = RegisterTransform()
            appExtension?.registerTransform(registerTransform)
        }
    }

    abstract fun createAdapter(): IPluginLaunchAdapter
}