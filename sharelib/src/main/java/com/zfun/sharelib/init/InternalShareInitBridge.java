package com.zfun.sharelib.init;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zfun.sharelib.SdkApiProvider;
import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.core.IShareHandler;
import com.zfun.sharelib.core.SinaWeiboHandler;
import com.sina.weibo.sdk.common.UiError;
import com.sina.weibo.sdk.openapi.IWBAPI;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.tencent.tauth.Tencent;

/**
 * 初始化API。<br/>
 * 一定要调用{@link #init(Context, boolean, com.zfun.sharelib.ShareMgrImpl.ShareTypeBuilder)}方法，否则无法正常运行，并且要对其返回值 {@link InitParams}进行设置。
 * <br/>
 * {@link #configDebug(IDebugCheck)}configxxx()方法可以设置一些个性化的东西。
 * <br/>
 * 在所有调用分享方法的Activity的{@link Activity#onActivityResult(int, int, Intent)}方法中调用{@link #onActivityResult(Activity, int, int, Intent)}方法。
 * <br/>
 * 微信回调需要WxEntryActivity承接，如果自己不需要监听就写一个自己【xxx.WxEntryActivity】继承{@link com.zfun.sharelib.WxCallbackActivity}并声明到Manifest中（xxx为自己app的包名），
 * 否则就在你的【微信回调Activity】中实例化{@link com.zfun.sharelib.WxCallbackActivity#WxCallbackActivity(Activity)}并且调用
 * {@link com.zfun.sharelib.WxCallbackActivity#onCreate(Bundle)
 *
 * <p>
 * Created by lzf on 2021/12/21 2:18 下午
 */
public class InternalShareInitBridge {
    private IHttpPicDownloader iHttpPicDownloader;
    private final InitParams initParams;
    private Context applicationContext;
    private IDebugCheck debugCheck;
    private IMessageHandler messageHandler;
    private IOptWxCallback optWxCallback;
    private ShareMgrImpl.ShareTypeBuilder supportShareTypeShareTypeBuilder;
    private Class<?> appEntryActivityClass;
    private boolean isPrivacyPolicyAgreed;
    private IToast tipToast;

    public InitParams init(@NonNull Context context, boolean isPrivacyPolicyAgreed, ShareMgrImpl.ShareTypeBuilder shareTypeBuilder) {
        /*checkInit();*/
        applicationContext = context;
        this.supportShareTypeShareTypeBuilder = shareTypeBuilder;
        this.isPrivacyPolicyAgreed = isPrivacyPolicyAgreed;
        return initParams;
    }

    public InternalShareInitBridge configDebug(IDebugCheck debugCheck) {
        this.debugCheck = debugCheck;
        return this;
    }

    public InternalShareInitBridge configMessageHandler(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }

    /**
     * @param appEntryActivityClass app的入口Activity
     */
    public InternalShareInitBridge configWxCallbackOpt(@Nullable IOptWxCallback wxCallback, @NonNull Class<?> appEntryActivityClass) {
        this.optWxCallback = wxCallback;
        this.appEntryActivityClass = appEntryActivityClass;
        return this;
    }

    public InternalShareInitBridge configPicDownloader(IHttpPicDownloader httpGet) {
        this.iHttpPicDownloader = httpGet;
        return this;
    }

    public InternalShareInitBridge configTipToast(@Nullable IToast toast) {
        this.tipToast = toast;
        return this;
    }

    public boolean onActivityResult(final Activity activity, final int requestCode, int resultCode, Intent data) {
        if (requestCode == SdkApiProvider.SINA_REQUEST_CODE) {
            try {
                // fix NPE
                SdkApiProvider.getWBAPI(activity).authorizeCallback(requestCode, resultCode, data);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == 10103 || requestCode == 10104 || requestCode == 11101) {//QQ好友分享,QQ空间分享,QQ登录授权
            if (null != data) { // data 为null的话是第三方分享界面“异常关闭”
                return Tencent.onActivityResultData(requestCode, resultCode, data, null);
            }
        }

        if (data != null && "com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY".equalsIgnoreCase(data.getAction())) {
            IWBAPI wbapi = SdkApiProvider.getWBAPI(activity);
            wbapi.doResultIntent(data, new WbShareCallback() {
                @Override
                public void onComplete() {
                    IShareHandler shareHandler = ShareMgrImpl.getInstance().getCurShareHandler();
                    if (shareHandler instanceof SinaWeiboHandler) {
                        ((SinaWeiboHandler) shareHandler).postShareSuccess();
                    }
                }

                @Override
                public void onError(UiError uiError) {
                    IShareHandler shareHandler = ShareMgrImpl.getInstance().getCurShareHandler();
                    if (shareHandler instanceof SinaWeiboHandler) {
                        ((SinaWeiboHandler) shareHandler).postShareError();
                    }
                }

                @Override
                public void onCancel() {
                    IShareHandler shareHandler = ShareMgrImpl.getInstance().getCurShareHandler();
                    if (shareHandler instanceof SinaWeiboHandler) {
                        ((SinaWeiboHandler) shareHandler).postShareCancel();
                    }
                }
            });
        }
        return false;
    }

    public void release() {

    }

    public InitParams getInitParams() {
        return initParams.copy();
    }

    @NonNull
    public Context getApplicationContext() {
        return applicationContext;
    }

    public boolean isPrivacyPolicyAgreed() {
        return isPrivacyPolicyAgreed;
    }

    @NonNull
    public IDebugCheck getDebugCheck() {
        if (null == debugCheck) {
            return NullableDebugCheck.get();
        }
        return debugCheck;
    }

    @NonNull
    public IMessageHandler getMessageHandler() {
        if (null == messageHandler) {
            return NullableMessageHandler.get();
        }
        return messageHandler;
    }

    @Nullable
    public IToast getTipToast() {
        return tipToast;
    }

    public ShareMgrImpl.ShareTypeBuilder getSupportShareTypeBuilder() {
        return supportShareTypeShareTypeBuilder;
    }

    @NonNull
    public IOptWxCallback getOptWxCallback(@NonNull Activity WxCallbackActivity) {
        if (null == optWxCallback) {
            return NullableOptWxCallback.get(WxCallbackActivity, appEntryActivityClass);
        }
        return optWxCallback;
    }

    @Nullable
    public IHttpPicDownloader getPicDownloader() {
        return iHttpPicDownloader;
    }

    //single instance
    private static volatile InternalShareInitBridge sInternalShareInitBridge;

    private InternalShareInitBridge() {
        initParams = new InitParams();
    }

    public static InternalShareInitBridge getInstance() {
        if (null == sInternalShareInitBridge) {
            synchronized (InternalShareInitBridge.class) {
                if (null == sInternalShareInitBridge) {
                    sInternalShareInitBridge = new InternalShareInitBridge();
                }
            }
        }
        return sInternalShareInitBridge;
    }
}
