package com.zfun.plugin.inject

import com.android.build.gradle.AppExtension
import com.zfun.plugin.inject.impl.InjectTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class InjectPlugin:Plugin<Project> {

    override fun apply(project: Project) {
        val appExtension = project.extensions.getByType(AppExtension::class.java)
        appExtension.registerTransform(TransformDelegate(project, InjectTransform()))
    }
}
