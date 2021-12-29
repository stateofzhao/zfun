package com.zfun.sharelib.init;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.SsoFactory;
import com.zfun.sharelib.core.IShareHandler;
import com.zfun.sharelib.core.SinaWeiboHandler;
import com.sina.weibo.sdk.common.UiError;
import com.sina.weibo.sdk.openapi.IWBAPI;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.tencent.tauth.Tencent;

import java.lang.ref.WeakReference;

/**
 * 初始化API。<br/>
 * 一定要调用{@link #init(Activity, boolean)}方法，否则无法正常运行，并且要对其返回值 {@link InitParams}进行设置。
 * <br/>
 * {@link #configDebug(IDebugCheck)}configxxx()方法可以设置一些个性化的东西。
 * <br/>
 * 在{@link #init(Activity, boolean)}方法中传入的Activity的{@link Activity#onActivityResult(int, int, Intent)}方法中调用
 * {@link #onActivityResult(Activity,int, int, Intent)}方法。
 * <br/>
 * 如果自己不需要实现微信回调Activity，就直接使用{@link com.zfun.sharelib.WxCallbackActivity}，
 * 否则就实例化{@link com.zfun.sharelib.WxCallbackActivity#WxCallbackActivity(Activity)}并且调用
 * {@link com.zfun.sharelib.WxCallbackActivity#onCreate(Bundle)
 *
 * <P>
 * Created by lzf on 2021/12/21 2:18 下午
 */
public class InitContext {

    public InitParams init(Activity mainActivity, boolean isPrivacyPolicyAgreed) {
        checkInit();
        hostActivity = new WeakReference<>(mainActivity);
        this.isPrivacyPolicyAgreed = isPrivacyPolicyAgreed;
        return initParams;
    }

    public boolean onActivityResult(final Activity activity,final int requestCode, int resultCode, Intent data){
        if (requestCode == SsoFactory.SINA_REQUEST_CODE) {
            if (SsoFactory.getWBAPI() != null) {
                try {
                    // fix NPE
                    SsoFactory.getWBAPI().authorizeCallback(requestCode,resultCode, data);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == 10103 || requestCode == 10104 || requestCode == 11101) {//QQ好友分享,QQ空间分享,QQ登录授权
            if(null != data){ // data 为null的话是第三方分享界面“异常关闭”
                return Tencent.onActivityResultData(requestCode, resultCode, data, null);
            }
        }

        if (data != null && "com.sina.weibo.sdk.action.ACTION_SDK_REQ_ACTIVITY".equalsIgnoreCase(data.getAction())) {
            IWBAPI wbapi = SsoFactory.getWBAPI(activity);
            if (wbapi != null) {
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
        }
        return false;
    }

    public InitContext configDebug(IDebugCheck debugCheck) {
        this.debugCheck = debugCheck;
        return this;
    }

    public InitContext configMessageHandler(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }

    public InitContext configWxCallbackOpt(@Nullable IOptWxCallback wxCallback, @NonNull Class<?> appEntryActivityClass){
        optWxCallback = wxCallback;
        this.appEntryActivityClass = appEntryActivityClass;
        return this;
    }

    public void release() {
        SsoFactory.destroyKugouLoginWBAPI();
    }

    public InitParams getInitParams() {
        return initParams.copy();
    }

    @NonNull
    public Activity getHostActivity() {
        if (null == hostActivity || null == hostActivity.get()) {
            return NullableActivity.get();
        }
        return hostActivity.get();
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

    public IMessageHandler getMessageHandler() {
        if (null == messageHandler) {
            return NullableMessageHandler.get();
        }
        return messageHandler;
    }

    public IOptWxCallback getOptWxCallback(@NonNull Activity WxCallbackActivity){
        if(null == optWxCallback){
            return NullableOptWxCallback.get(WxCallbackActivity, appEntryActivityClass);
        }
        return optWxCallback;
    }

    private void checkInit() {
        if (null != hostActivity && null != hostActivity.get()) {
            throw new RuntimeException("sharelib：重复初始化");
        }
    }

    private static volatile InitContext sInitContext;

    public static InitContext getInstance() {
        if (null == sInitContext) {
            synchronized (InitContext.class) {
                if (null == sInitContext) {
                    sInitContext = new InitContext();
                }
            }
        }
        return sInitContext;
    }

    private final InitParams initParams;
    private WeakReference<Activity> hostActivity;
    private IDebugCheck debugCheck;
    private IMessageHandler messageHandler;
    private IOptWxCallback optWxCallback;
    private Class<?> appEntryActivityClass;
    private boolean isPrivacyPolicyAgreed;

    private InitContext() {
        initParams = new InitParams();
    }
}
