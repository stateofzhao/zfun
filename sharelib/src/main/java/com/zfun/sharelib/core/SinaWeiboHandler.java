package com.zfun.sharelib.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;
import android.text.TextUtils;

import com.zfun.sharelib.AccessTokenUtils;
import com.zfun.sharelib.LiveState;
import com.zfun.sharelib.SdkApiProvider;
import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.init.InternalShareInitBridge;
import com.zfun.sharelib.init.NullableToast;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.common.UiError;
import com.sina.weibo.sdk.openapi.IWBAPI;

/**
 * <p/>
 * Created by zfun on 2017/8/8 16:41
 */
public class SinaWeiboHandler implements IShareHandler {
    private Context mContext;
    private boolean isRelease = true;

    @Nullable
    private ShareData mNowShareData;//注意内存泄漏，这个handler是单例

    public void postShareSuccess() {
        final String msg = "分享成功";
        if(null == mNowShareData){
            NullableToast.showSysToast(msg);
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        final ShareData.OnShareListener listener = mNowShareData.mShareListener;
        if(null != listener){
            InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(() -> {
                listener.onSuccess(msg);
                mNowShareData.mShareListener = null;
                mNowShareData = null;
            });
        } else {
            mNowShareData = null;
        }
        ShareMgrImpl.getInstance().clearCurShareHandler();
        NullableToast.showSysToast("分享成功");
    }

    public void postShareError(String msg) {
        if(null == mNowShareData){
            if (!TextUtils.isEmpty(msg)){
                NullableToast.showSysToast(msg);
            }
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        final ShareData.OnShareListener listener = mNowShareData.mShareListener;
        if(null != listener){
            InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(() -> {
                listener.onFail(msg);
                mNowShareData.mShareListener = null;
                mNowShareData = null;
            });
        }else {
            mNowShareData = null;
        }
        if (!TextUtils.isEmpty(msg)){
            NullableToast.showSysToast(msg);
        }
        ShareMgrImpl.getInstance().clearCurShareHandler();
    }

    public void postShareCancel() {
        final String msg = "取消分享";
        if(null == mNowShareData){
            NullableToast.showSysToast(msg);
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        final ShareData.OnShareListener listener = mNowShareData.mShareListener;
        if(null != listener){
            InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(() -> {
                listener.onCancel(msg);
                mNowShareData.mShareListener = null;
                mNowShareData = null;
            });
        }else {
            mNowShareData = null;
        }
        NullableToast.showSysToast(msg);
        ShareMgrImpl.getInstance().clearCurShareHandler();
    }

    @Override
    public void share(@NonNull final ShareData shareData) {
        if (isRelease) {
            return;
        }
        if (!InternalShareInitBridge.getInstance().isPrivacyPolicyAgreed()) {
            return;
        }

        if (!LiveState.getInstance().isNetAvailable()) {
            postShareError("网络连接不可用");
            return;
        }
        if (null == mContext) {
            return;
        }
        final IWBAPI realWBAPI = SdkApiProvider.getWBAPI(mContext);

        Oauth2AccessToken accessToken = AccessTokenUtils.readAccessToken(mContext);
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
            realWBAPI.shareMessage(message, false);
        } else {
            //授权回调 需要在与SsoHandler绑定的Activity的onActivityResult()中做处理了，才能回调到这里注册接口，这个SDK真是醉了
            realWBAPI.authorize(new WbAuthListener() {
                @Override
                public void onComplete(Oauth2AccessToken oauth2AccessToken) {
                    try {
                        String webUid = oauth2AccessToken.getUid();
                        String token = oauth2AccessToken.getAccessToken();
                        String expiresIn = String.valueOf(oauth2AccessToken.getExpiresTime() / 1000);
                        AccessTokenUtils.keepAccessToken(mContext, oauth2AccessToken);
                        AccessTokenUtils.keepAccessUid(mContext, webUid);
                        NullableToast.showSysToast("认证成功");
                    } catch (Exception e) {
                        postShareError("分享失败");
                    }
                }

                @Override
                public void onError(UiError uiError) {
                    postShareError(uiError.errorMessage);
                }

                @Override
                public void onCancel() {
                    postShareCancel();
                }
            });
        }
    }

    @Override
    public boolean isSupport() {
        return true;
    }

    @Override
    public void init() {
        isRelease = false;
        mContext = InternalShareInitBridge.getInstance().getApplicationContext();
    }

    @Override
    public void release() {
        isRelease = true;
        mContext = null;
    }
}
