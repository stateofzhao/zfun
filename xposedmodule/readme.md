## 简介
一个Xposed模块，用来检测方法调用的。

## 用法
在要检测的项目的`asset`目录下新建`detect_methods.json`文件，按照下面的格式来声明要检测的方法调用：
```json
[
    {
        "className":"android.app.ApplicationPackageManager",
        "methodName":"queryIntentActivities",
        "args":["android.content.Intent","int"]
    },
    {
        "className":"android.app.ApplicationPackageManager",
        "methodName":"getPackageInfo",
        "args":["java.lang.String","int"]
    },
    ...
]
```
- className 方法所在的类名
- methodName 方法名
- args 方法参数，注意顺序必须与方法的参数顺序一致，并且注意要区分`Integer`和`int`等装箱数据类型和原始数据类型。

## 验证
通过看Logcat控制台的输出来验证方法调用情况，可以通过你app的包名来过滤日志。
如果只是纯净的看方法调用堆栈，通过`zfun`来过滤。