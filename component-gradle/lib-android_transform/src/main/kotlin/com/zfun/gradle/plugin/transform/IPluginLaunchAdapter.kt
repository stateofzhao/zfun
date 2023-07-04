package com.zfun.gradle.plugin.transform

import org.objectweb.asm.ClassVisitor

interface IPluginLaunchAdapter {
    /** Transform名称，会显示在gradle task列表上 */
    fun transformName(): String

    /** 打印debug日志时带上的 */
    fun pluginName(): String {
        return "lib-android_transform"
    }

    //----------与注解处理器生成的类对应上，此处根据注解处理器生成的类的特征来获取要插桩的类
    /**
     * 类似： com/zfun/init/inits
     * */
    fun annotationCompilerGenerateClassPackageName(): String

    /**
     * 类似： com\\\\zfun\\\\init\\\\inits
     * */
    fun annotationCompilerGenerateClassPackageNameForWindows(): String

    //----------xxx-api模块中相关类，在上面匹配完后，会再次根据xxx-api中接口名来获取要被插入到 injectTargetClass 中的代码
    /**
     * 不带包名，就是简单类名
     *
     * 类似：IInitProvider
     * */
    fun apiProviderClassSimpleNames(): Array<String>

    /**
     * 类似：com/zfun/initapi/internal
     *
     * 注意：必须与 apiProviderClassSimpleName() 方法返回值对应上！
     * */
    fun apiProviderClassPackageNames():Array<String>


    //----------要在编译期修改的类
    /**
     * 类似：com/zfun/initapi/InitMgr
     * */
    fun injectTargetClassName(): String

    /**
     * 类似：com/zfun/initapi/InitMgr.class
     * */
    fun injectTargetClassFileName(): String

    //
    fun isDebug():Boolean{
        return false
    }

    //-----------ASM字节码修改
    /**
     * 如果无特殊需求，可以使用{@link ClassVisitorForStaticInjectBlock}
     * */
    fun createASMClassVisitor(api: Int, cv: ClassVisitor): ClassVisitor

}