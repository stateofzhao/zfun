package com.zfun.sharelib.init;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.core.ShareConstant;

/**
 *
 * 如果要启用微信分享，需要在项目的build.gradle引入微信分享SDK:
 * com.tencent.mm.opensdk:wechat-sdk-android-without-mta:6.8.0
 * <P/>
 * 如果要启用微博分享，需要在项目中引入SDK：
 * openDefault-10.10.0
 * <P/>
 *
 * 初始化API。<br/>
 * 一定要调用{@link #init(Context, boolean, com.zfun.sharelib.ShareMgrImpl.ShareTypeBuilder)}方法，否则无法正常运行，并且要对其返回值 {@link InitParams}进行设置。<br/>
 * {@link #configDebug(IDebugCheck)}configxxx()方法可以设置一些个性化的东西。<br/>
 * 在所有调用分享方法的Activity的{@link Activity#onActivityResult(int, int, Intent)}方法中调用{@link #onActivityResult(Activity, int, int, Intent)}方法。<br/>
 * <P/>
 * -------微信分享注意---------- start
 * 微信回调需要WxEntryActivity承接，如果自己不需要监听就写一个自己【xxx.wxapi.WXEntryActivity】继承{@link com.zfun.sharelib.WxCallbackActivity}并声明到Manifest中（xxx为自己app的包名），
 * 否则就在你的【微信回调Activity】的onCreate()方法中调用{@link ShareInitBuilder#wxEntryActivityOnCreate(Activity)}方法。
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

    public static void wxEntryActivityOnCreate(Activity activity){
        IWXAPI api = WXAPIFactory.createWXAPI(activity, ShareConstant.WX_APP_ID, true);
        api.registerApp(ShareConstant.WX_APP_ID);
        api.handleIntent(activity.getIntent(), new IWXAPIEventHandler() {
            @Override
            public void onReq(BaseReq baseReq) {
                InternalShareInitBridge.getInstance().getOptWxCallback(activity).onOptWxReq(baseReq);
            }

            @Override
            public void onResp(BaseResp baseResp) {
                InternalShareInitBridge.getInstance().getOptWxCallback(activity).onOptWxResp(baseResp);
            }
        });
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
