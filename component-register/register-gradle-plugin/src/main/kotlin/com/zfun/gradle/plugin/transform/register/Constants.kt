package com.zfun.gradle.plugin.transform.register

object Constants {
    const val TRANSFORM_NAME = "__ZfunRegister__"

    //下面这些东东是需要与 annotation compile 生成的类对应上的
    const val ANNOTATION_PROCESSOR_GENERATE_PACKAGE_NAME = "com/zfun/register/registers"
    const val ANNOTATION_PROCESSOR_GENERATE_PACKAGE_NAME_WINDOWS = "com\\\\zfun\\\\register\\\\registers"
    //init-api 中的标记类
    val REGISTER_COMPONENT_REGISTER_PROVIDER = "IRegisterProvider"
    val REGISTER_COMPONENT_REGISTER_PROVIDER_PACKAGE = "com/zfun/register"
    val REGISTER_COMPONENT_NAMES = arrayOf(REGISTER_COMPONENT_REGISTER_PROVIDER)
    val REGISTER_COMPONENT_PACKAGES = arrayOf(REGISTER_COMPONENT_REGISTER_PROVIDER_PACKAGE)
    //插入代码的类
    const val INJECT_CODE_CLASS_NAME = "com/zfun/register/RegisterCenter"
    const val INJECT_CODE_CLASS_FILE_NAME = "$INJECT_CODE_CLASS_NAME.class"
    const val INJECT_CODE_CLASS_FIELD_MAP_NAME = "sRegisterProviderMap"
    const val INJECT_CODE_CLASS_FIELD_LIST_NAME = "sRegisterProviderList"
}