package com.zfun.gradle.plugin.transform.register

import com.zfun.gradle.plugin.transform.IPluginLaunchAdapter
import com.zfun.gradle.plugin.transform.AbsPluginLaunch
import com.zfun.gradle.plugin.transform.utils.ClassVisitorForStaticInjectBlock
import org.objectweb.asm.ClassVisitor

class MyPlugin : AbsPluginLaunch() {
    override fun createAdapter(): IPluginLaunchAdapter {
        return object : IPluginLaunchAdapter {
            override fun transformName(): String {
                return Constants.TRANSFORM_NAME
            }

            override fun pluginName(): String {
                return "RegisterCenter"
            }

            override fun annotationCompilerGenerateClassPackageName(): String {
                return Constants.ANNOTATION_PROCESSOR_GENERATE_PACKAGE_NAME
            }

            override fun annotationCompilerGenerateClassPackageNameForWindows(): String {
                return Constants.ANNOTATION_PROCESSOR_GENERATE_PACKAGE_NAME_WINDOWS
            }

            override fun apiProviderClassSimpleNames(): Array<String> {
                return Constants.REGISTER_COMPONENT_NAMES
            }

            override fun apiProviderClassPackageNames(): Array<String> {
                return Constants.REGISTER_COMPONENT_PACKAGES
            }

            override fun injectTargetClassName(): String {
                return Constants.INJECT_CODE_CLASS_NAME
            }

            override fun injectTargetClassFileName(): String {
                return Constants.INJECT_CODE_CLASS_FILE_NAME
            }

            override fun createASMClassVisitor(api: Int, cv: ClassVisitor): ClassVisitor {
                return ClassVisitorForStaticInjectBlock(api,cv,MyMethodOpt())
            }

        }
    }
}