package com.zfun.gradle.plugin.transform.init

object Constants {
    const val TRANSFORM_NAME = "__ZfunInit__"

    //下面这些东东是需要与 annotation compile 生成的类对应上的
    const val ANNOTATION_PROCESSOR_GENERATE_PACKAGE_NAME = "com/zfun/init/inits"
    //init-api 中的标记类
    const val INIT_COMPONENT_NAME = "IInitProvider"
    const val INIT_COMPONENT_PACAGE = "com/zfun/initapi/internal"
    //插入代码的类
    const val INJECT_CODE_CLASS_NAME = "com/zfun/initapi/InitMgr"
    const val INJECT_CODE_CLASS_FILE_NAME = "$INJECT_CODE_CLASS_NAME.class"
}