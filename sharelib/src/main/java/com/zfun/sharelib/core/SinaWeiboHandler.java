package com.zfun.sharelib.core;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;

import com.zfun.sharelib.AccessTokenUtils;
import com.zfun.sharelib.LiveState;
import com.zfun.sharelib.SsoFactory;
import com.zfun.sharelib.init.InitContext;
import com.zfun.sharelib.init.NullableToast;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.common.UiError;
import com.sina.weibo.sdk.openapi.IWBAPI;

/**
 * 新浪微博分享，注意微博分享结果的回调在{@link InitContext#getHostActivity()} 或者是 {@link ShareData#setCompelContext(Activity)}
 * 对应的Activity中。
 * <p/>
 * Created by zfun on 2017/8/8 16:41
 */
public class SinaWeiboHandler implements IShareHandler {
    private Activity mActivity;
    private boolean isRelease = true;

    @Nullable
    private ShareData mNowShareData;//注意内存泄漏，这个handler是单例

    public void postShareSuccess() {
        if (null != mNowShareData && null != mNowShareData.mShareListener) {
            InitContext.getInstance().getMessageHandler().asyncRun(new Runnable() {
                @Override
                public void run() {
                    mNowShareData.mShareListener.onSuccess();
                    mNowShareData.mShareListener = null;
                    mNowShareData = null;
                }
            });
        } else {
            mNowShareData = null;
        }
        NullableToast.showSysToast("分享成功");
    }

    public void postShareError() {
        if (null != mNowShareData && null != mNowShareData.mShareListener) {
            InitContext.getInstance().getMessageHandler().asyncRun(new Runnable() {
                @Override
                public void run() {
                    mNowShareData.mShareListener.onFail();
                    mNowShareData.mShareListener = null;
                    mNowShareData = null;
                }
            });
        } else {
            mNowShareData = null;
        }
        NullableToast.showSysToast("发送失败");
    }

    public void postShareCancel() {
        if (null != mNowShareData && null != mNowShareData.mShareListener) {
            InitContext.getInstance().getMessageHandler().asyncRun(new Runnable() {
                @Override
                public void run() {
                    mNowShareData.mShareListener.onCancel();
                    mNowShareData.mShareListener = null;
                    mNowShareData = null;
                }
            });
        } else {
            mNowShareData = null;
        }
        NullableToast.showSysToast("发送取消");
    }

    @Override
    public void share(@NonNull final ShareData shareData) {
        if (isRelease) {
            return;
        }
        if (!InitContext.getInstance().isPrivacyPolicyAgreed()) {
            return;
        }
        final Activity compelActivity = shareData.getCompelContext();//注意，不能设为全局属性，因为SinaWeiboHandler是app运行期间一直存在的，会引起Activity泄露
        if (null == mActivity && null == compelActivity) {
            return;
        }

        final Activity realActivity = null != compelActivity ? compelActivity : mActivity;
        final IWBAPI realWBAPI;

        if (null != shareData.getWBAPI()) {
            realWBAPI = shareData.getWBAPI();
        } else if (null != mActivity) {
            realWBAPI = SsoFactory.getWBAPI(mActivity);
        } else {
            realWBAPI = null;
        }

        if (!LiveState.getInstance().isNetAvailable()) {
            NullableToast.showSysToast("网络连接不可用");
            return;
        }

        Oauth2AccessToken accessToken = AccessTokenUtils.readAccessToken(realActivity);
        if (accessToken.isSessionValid()) {
            mNowShareData = shareData;
            WeiboMultiMessage message = new WeiboMultiMessage();

            TextObject textObject = new TextObject();
            textObject.text = shareData.getSinaShareData().msg;
            message.textObject = textObject;

            ImageObject imageObject = new ImageObject();
            if (!TextUtils.isEmpty(shareData.getSinaShareData().imagePath)) {
                imageObject.setImagePath(shareData.getSinaShareData().imagePath);
            } else if (null != shareData.getSinaShareData().bitmap) {
                imageObject.setImageData(shareData.getSinaShareData().bitmap);
            }

            message.imageObject = imageObject;
            if (realWBAPI != null) {
                realWBAPI.shareMessage(message, false);
            }
        } else {
            if (realWBAPI != null) {
                //授权回调 需要在与SsoHandler绑定的Activity的onActivityResult()中做处理了，才能回调到这里注册接口，这个SDK真是醉了
                realWBAPI.authorize(new WbAuthListener() {
                    @Override
                    public void onComplete(Oauth2AccessToken oauth2AccessToken) {
                        try {
                            String webUid = oauth2AccessToken.getUid();
                            String token = oauth2AccessToken.getAccessToken();
                            String expiresIn = String.valueOf(oauth2AccessToken.getExpiresTime() / 1000);
                            AccessTokenUtils.keepAccessToken(mActivity, oauth2AccessToken);
                            AccessTokenUtils.keepAccessUid(mActivity, webUid);
                            NullableToast.showSysToast("认证成功");
                        } catch (Exception e) {
                            postShareError();
                        }
                    }

                    @Override
                    public void onError(UiError uiError) {
                        postShareError();
                    }

                    @Override
                    public void onCancel() {
                        postShareCancel();
                    }
                });
            }
        }
    }

    @Override
    public boolean isSupport() {
        return true;
    }

    @Override
    public void init() {
        isRelease = false;
        mActivity = InitContext.getInstance().getHostActivity();
    }

    @Override
    public void release() {
        isRelease = true;
        mActivity = null;
    }
}
