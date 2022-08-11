package com.zfun.sharelib.init;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.WxCallbackActivity;

/**
 *
 * 如果要启用微信分享，需要在项目的build.gradle引入微信分享SDK:
 * com.tencent.mm.opensdk:wechat-sdk-android-without-mta:6.8.0
 * <P/>
 * 如果要启用微博分享，需要在项目中引入SDK：
 * openDefault-10.10.0
 * <P/>
 *
 * -------微信分享注意---------- start
 * 微信回调需要WxEntryActivity承接，如果自己不需要监听就写一个自己【xxx.wxapi.WXEntryActivity】继承{@link com.zfun.sharelib.WxCallbackActivity}并声明到Manifest中（xxx为自己app的包名），
 * 否则就在你的【微信回调Activity】中实例化{@link com.zfun.sharelib.WxCallbackActivity#WxCallbackActivity(Activity)}并且调用
 * {@link com.zfun.sharelib.WxCallbackActivity#onCreate(Bundle)
 *-------微信分享注意---------- end
 *
 * -------qq分享注意---------- start
 * 切记如果使用qq分享，需要在项目主工程的build.gradle中添加
 * <code>
 *     android{
 *         defaultConfig{
 *              manifestPlaceholders = [
 *                 QQ_APP_ID:xxxxx
 *              ]
 *         }
 *     }
 * </code>
 * 其中xxxx为你的 QQ_APP_ID。
 * -------qq分享注意---------- end
 */
public class ShareInitBuilder {
    private InitParams mInitParams;
    private Context mContext;
    private boolean mIsPrivacyPolicyAgreed;
    private IDebugCheck mDebugCheck;
    private IMessageHandler mMessageHandler;
    private IOptWxCallback mWxCallbackOpt;
    private IHttpPicDownloader mPicDownloader;
    private Class<?> mEntryActivityClass;
    private ShareMgrImpl.ShareTypeBuilder mShareTypeBuilder;
    private IToast mToast;

    private ShareInitBuilder() {
    }

    public static ShareInitBuilder initParams(@NonNull Context context,@NonNull InitParams initParams,@NonNull ShareMgrImpl.ShareTypeBuilder shareTypeBuilder) {
        ShareInitBuilder builder = new ShareInitBuilder();
        builder.mInitParams = initParams;
        builder.mShareTypeBuilder = shareTypeBuilder;
        builder.mContext = context;
        return builder;
    }

    public static boolean onActivityResult(final Activity activity,final int requestCode, int resultCode, Intent data){
        return InternalShareInitBridge.getInstance().onActivityResult(activity,requestCode,resultCode,data);
    }

    public static void wxEntryActivityOnCreate(Bundle bundle){
        new WxCallbackActivity().onCreate(bundle);
    }

    public ShareInitBuilder privacyPolicyAgreed(boolean agreed) {
        mIsPrivacyPolicyAgreed = agreed;
        return this;
    }

    public ShareInitBuilder debugCheck(IDebugCheck debugCheck) {
        mDebugCheck = debugCheck;
        return this;
    }

    public ShareInitBuilder toast(@Nullable IToast toast){
        mToast = toast;
        return this;
    }

    public ShareInitBuilder wxCallbackOpt(IOptWxCallback callbackOpt) {
        mWxCallbackOpt = callbackOpt;
        return this;
    }

    public ShareInitBuilder picDownloader(@NonNull IHttpPicDownloader downloader) {
        mPicDownloader = downloader;
        return this;
    }

    public ShareInitBuilder entryActivity(@NonNull Class<?> entryActivityClass) {
        mEntryActivityClass = entryActivityClass;
        return this;
    }

    public ShareInitBuilder messageHandler(@NonNull IMessageHandler messageHandler) {
        mMessageHandler = messageHandler;
        return this;
    }

    public void build() {
        if (null == mInitParams) {
            throw new IllegalArgumentException("InitParams must set");
        }
        if (null == mMessageHandler) {
            throw new IllegalArgumentException("MessageHandler must set");
        }
        if (null == mPicDownloader) {
            throw new IllegalArgumentException("HttpGet must set");
        }
        InitParams initParams = InternalShareInitBridge.getInstance().init(mContext, mIsPrivacyPolicyAgreed,mShareTypeBuilder);
        initParams.from(mInitParams);
        InternalShareInitBridge.getInstance()
                .configDebug(mDebugCheck)
                .configPicDownloader(mPicDownloader)
                .configMessageHandler(mMessageHandler)
                .configTipToast(mToast)
                .configWxCallbackOpt(mWxCallbackOpt, mEntryActivityClass);
    }
}
