# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#分为4个步骤：
#1，压缩（shrink）：移除未使用的类、方法、字段等；
#2，优化（optimize）：优化字节码、简化代码等操作；
#3，混淆（obfuscate）：使用简短的、无意义的名称重全名类名、方法名、字段等；
#4，预校验（preverify）：为class添加预校验信息。

# 不压缩，默认情况下，除了-keep配置（下详）的类及其直接或间接引用到的类，都会被移除
-dontshrink
# 不优化，默认开启
-dontoptimize
# 不混淆，默认开启
-dontobfuscate

# 不对class进行预校验，默认情况下，在编译版本为micro或者1.6或更高版本时是开启的。但编译成Android版本时，预校验是不必须的，配置这个选项可以节省一点编译时间。
#（Android会把class编译成dex，并对dex文件进行校验，对class进行预校验是多余的。）
-dontpreverify

