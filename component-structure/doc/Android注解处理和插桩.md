# Android注解处理-APT(Annotation Processing Tool)
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

# Android字节码插桩
方案有三个：
AspectJ
框架，自己无需实现字节码处理，只需要在代码中使用注解即可，所以其无法全部批量插桩，只能手动在需要插桩的地方添加注解。
AspectJ会额外生成一些包装代码，对性能以及包大小有一定影响。
Javassist
ASM