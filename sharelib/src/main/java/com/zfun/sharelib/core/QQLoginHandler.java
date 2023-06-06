package com.zfun.sharelib.core;


import android.app.Activity;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zfun.sharelib.AccessTokenUtils;
import com.zfun.sharelib.SdkApiProvider;
import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.init.InternalShareInitBridge;
import com.zfun.sharelib.init.NullableToast;
import com.zfun.sharelib.type.QzoneOAuthV2;

import org.json.JSONObject;

public class QQLoginHandler extends QQShareAbsHandler{

    private TencentLoginListener listener;

    @Override
    public void share(@NonNull ShareData shareData) {
        if (isRelease) {//防止 由于异步任务执行完毕后，回调这里，但是ShareMgrImpl已经被relase掉了，而出现的NullPointException
            return;
        }
        if (null == mContext) {
            return;
        }
        final Activity activity = shareData.getQQLoginData().activityRef.get();
        if(null == activity){
            return;
        }
        final Tencent tencent = SdkApiProvider.getTencentAPI(mContext);

        final QzoneOAuthV2 qzoneOAuth = AccessTokenUtils.doReadTencentQzoneToken(mContext);
        if (!TextUtils.isEmpty(qzoneOAuth.openId) && !TextUtils.isEmpty(qzoneOAuth.accessToken) && !TextUtils.isEmpty(qzoneOAuth.expiresIn)) {
            tencent.setOpenId(qzoneOAuth.openId);
            tencent.setAccessToken(qzoneOAuth.accessToken, qzoneOAuth.expiresIn);
        }

        /*HashMap<String, Object> params = new HashMap<String, Object>();
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            params.put(KEY_RESTORE_LANDSCAPE, true);
        }
        params.put(KEY_SCOPE, "all");
        params.put(KEY_QRCODE, false);
        params.put(KEY_ENABLE_SHOW_DOWNLOAD_URL, mShowWebDownloadUi.isChecked());
        mTencent.login(this, loginListener, params);*/
        listener = new TencentLoginListener(shareData);
        tencent.login(activity, ShareConstant.QZONE_SCOPE, listener);
    }

    @Nullable
    @Override
    public IUiListener getUiListener() {
        return listener;
    }

    private class TencentLoginListener implements IUiListener {
        private final ShareData shareData;

        private TencentLoginListener(ShareData shareData){
            this.shareData = shareData;
        }

        @Override
        public void onCancel() {
            listener = null;
            postShareCancel(shareData,"取消授权");
        }

        @Override
        public void onComplete(Object data) {
            listener = null;
            if (isRelease) {
                return;
            }
            if (null == data){
                postShareError(shareData,"授权失败");
                return;
            }
            JSONObject jdata = (JSONObject) data;
            try {
                int ret = jdata.getInt("ret");
                String accessToken = jdata.optString(Constants.PARAM_ACCESS_TOKEN, "");
                String openid = jdata.optString(Constants.PARAM_OPEN_ID, "");
                String expiresIn = jdata.optString(Constants.PARAM_EXPIRES_IN, "");
                QzoneOAuthV2 auth = AccessTokenUtils.doReadTencentQzoneToken(mContext.getApplicationContext());
                auth.accessToken = accessToken;
                auth.openId = openid;
                auth.expiresIn = expiresIn;
                AccessTokenUtils.doSaveTencentQzoneToken(mContext, auth);
                if (!TextUtils.isEmpty(accessToken) && !TextUtils.isEmpty(expiresIn)
                        && !TextUtils.isEmpty(openid)) {
                    final Tencent tencent = SdkApiProvider.getTencentAPI(mContext);
                    tencent.setAccessToken(accessToken, expiresIn);
                    tencent.setOpenId(openid);
                }
                postShareSuccess(shareData,auth,"授权成功");
            } catch (Exception e) {
                postShareError(shareData,"授权失败");
            }
            /*final Context context = mContext.getApplicationContext();
            if (context != null) {
                com.tencent.connect.UserInfo mInfo = new com.tencent.connect.UserInfo(context,SdkApiProvider.getTencentAPI(mContext).getQQToken());
                mInfo.getUserInfo(new IUiListener() {

                    @Override
                    public void onError(UiError arg0) {
                    }

                    @Override
                    public void onComplete(Object arg0) {
                        if (isRelease) {
                            return;
                        }
                        JSONObject jsonObject = (JSONObject) arg0;
                        String name = jsonObject.optString("nickname");
                        if (!TextUtils.isEmpty(name)) {
                            AccessTokenUtils.doSaveUserInfoByType(context, null, name, AccessTokenUtils.SOURCE_QZONE);
                        }
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onWarning(int i) {
                    }
                });
            }*/
        }

        @Override
        public void onError(UiError arg0) {
            listener = null;
            if (isRelease) {
                return;
            }
            postShareError(shareData,arg0.errorCode+" == "+arg0.errorDetail);
        }

        @Override
        public void onWarning(int i) {
            listener = null;
        }
    }//

    //发送分享结果
    private void postShareSuccess(final ShareData shareData,final QzoneOAuthV2 authV2,final String msg) {
        if (!TextUtils.isEmpty(msg)){
            NullableToast.showSysToast(msg);
        }
        if (null == shareData) {
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        final ShareData.OnQQLoginListener callback = shareData.mQQLoginListener;
        if (null == callback) {
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(() -> {
            callback.onSuc(authV2,msg);
            shareData.mQQLoginListener = null;
            ShareMgrImpl.getInstance().clearCurShareHandler();
        });
    }

    private void postShareCancel(final ShareData shareData,final String msg) {
        if (!TextUtils.isEmpty(msg)){
            NullableToast.showSysToast(msg);
        }
        if (null == shareData) {
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        final ShareData.OnQQLoginListener callback = shareData.mQQLoginListener;
        if (null == callback) {
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(() -> {
            callback.onCancel(msg);
            shareData.mQQLoginListener = null;
            ShareMgrImpl.getInstance().clearCurShareHandler();
        });
    }

    private void postShareError(final ShareData shareData,final String msg) {
        if (!TextUtils.isEmpty(msg)){
            NullableToast.showSysToast(msg);
        }
        if (null == shareData) {
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        final ShareData.OnQQLoginListener callback = shareData.mQQLoginListener;
        if (null == callback) {
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(() -> {
            callback.onFail(msg);
            shareData.mQQLoginListener = null;
            ShareMgrImpl.getInstance().clearCurShareHandler();
        });
    }
}
