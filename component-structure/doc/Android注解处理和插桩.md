# Android注解
需要两个java类型library来配合实现：
- annotation library
- annotationProcessor library

## annotation library
顾名思义，专门用来定义注解的，参见：`:component-structure:annotation`。

## annotationProcessor library
顾名思义，用来处理 annotation library 中定义的注解的，根据 annotation library 中定义的注解来**生成java类**，注意，这个只能生成类而不对已有的类进行修改，对已有类进行修改这个是android插桩的范畴。

annotationProcessor library 需要依赖于 annotation library。

## 调用方
调用方需要同时依赖于 annotation library 和 annotationProcessor library。
- 对于 annotation library 正常依赖，使用implementation等来依赖。
- 对于 annotationProcessor library ，则需要专门的配置项：
    - java环境：annotationProcessor project('xxx')
    - kotlin环境：kapt project('xxx')

