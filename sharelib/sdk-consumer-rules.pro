#此处添加防止二次混淆时被混淆的文件，例如，自己通过反射用到的类，枚举等，具体哪些不能混淆可看看官方文档。
#如果没有特殊要保留的类，可以不用配置，即使自身混淆过的类又被二次混淆了也没关系。
#
#下述三方sdk提供的混淆规则，需要保留，防止二次混淆时造成这些三方sdk自己通过反射拿不到自己的类
#======== qqsdk ================================start
-keep class com.tencent.open.** {*;}
-keep class com.tencent.connect.** {*;}
-keep class com.tencent.tauth.** {*;}
#======== qqsdk ================================end

#======== 微博sdk ================================start
-keep public class com.sina.weibo.sdk.**{*;}
#======== 微博sdk ================================end

#======== 微信sdk ================================start
-keep class com.tencent.mm.opensdk.** {
    *;
}

-keep class com.tencent.wxop.** {
    *;
}

-keep class com.tencent.mm.sdk.** {
    *;
}
#======== 微信sdk ================================end