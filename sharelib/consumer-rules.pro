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

#======== me ================================start
#-keep class com.zfun.sharelib.ShareMgrImpl
#-keep class com.zfun.sharelib.ShareMgrImpl$ShareTypeBuilder{*;}
#-keep class com.zfun.sharelib.init.**{*;}
#-keep class com.zfun.sharelib.ShareUtils{*;}
##-keep class  com.zfun.sharelib.core.ShareData #保留ShareData类
#-keep class com.zfun.sharelib.core.ShareData{*;} #保留ShareData类及其中所有方法
##-keep class com.zfun.sharelib.core.ShareData$* #保留ShareData的所有内部类
#-keep class com.zfun.sharelib.core.ShareData$*{*;}#保留ShareData的所有内部类及其内部类方法
#-keep class com.zfun.sharelib.WxCallbackActivity
#-keep class com.zfun.sharelib.type.QzoneOAuthV2
#======== me ================================end