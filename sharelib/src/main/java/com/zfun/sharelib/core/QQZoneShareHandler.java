package com.zfun.sharelib.core;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.text.TextUtils;

import com.zfun.sharelib.AccessTokenUtils;
import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.init.InternalShareInitBridge;
import com.zfun.sharelib.init.NullableToast;
import com.zfun.sharelib.type.QzoneOAuthV2;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * QQ空间分享
 * <p/>
 * Created by lizhaofei on 2017/8/4.
 */
//这个应该有两种形式，一种是需要打开一个新的Fragment来让用户编辑 简介；一种是直接发送到QQ中。
public class QQZoneShareHandler extends QQShareAbsHandler {
    private static final String TAG = "QQZoneShareHandler";

    private QzoneOAuthV2 mQzoneOAuth = null;
    private TencentLoginListener mLoginListener;

    @Override
    public void share(@NonNull final ShareData shareData) {
        if (isRelease) {//防止 由于异步任务执行完毕后，回调这里，但是ShareMgrImpl已经被relase掉了，而出现的NullPointException
            return;
        }
        final Activity compelActivity = shareData.getCompelContext();
        if (null == mActivity && null == compelActivity) {
            return;
        }
        final Activity realActivity = null != compelActivity ? compelActivity : mActivity;//优先取强制使用的Activity,不会造成Activity泄露因为只是局部使用此Activity
        final Context applicationContext = realActivity.getApplicationContext();
        final Tencent tencent = ShareMgrImpl.getInstance().getTencent();

        mQzoneOAuth = AccessTokenUtils.doReadTencentQzoneToken(applicationContext);
        if (AccessTokenUtils.isSessionValid(mQzoneOAuth.expiresIn) && !"".equals(mQzoneOAuth.openId)) {
            tencent.setOpenId(mQzoneOAuth.openId);
            tencent.setAccessToken(mQzoneOAuth.accessToken, mQzoneOAuth.expiresIn);

            Bundle params = getShareParams(shareData);
            if (null == params) {
                postShareError(shareData);
                return;
            }
            TencentShareListener listener = new TencentShareListener(shareData);
            listener.activity = realActivity;
            listener.tencent = tencent;
            tencent.shareToQzone(realActivity, params, listener);
        } else {
            mLoginListener = new TencentLoginListener(TencentLoginListener.SOURCE_SHARE, shareData);
            tencent.login(realActivity, ShareConstant.QZONE_SCOPE, mLoginListener);
        }
    }

    private Bundle getShareParams(ShareData shareData) {
        ShareData.QQZone qqZone = shareData.getQqZShareData();

        if (null == qqZone) {
            return null;
        }

        Bundle params = new Bundle();
        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);//qq空间 目前仅支持 图文分享
        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, qqZone.title);
        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, qqZone.targetUrl);

        if (!TextUtils.isEmpty(qqZone.summary)) {
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, qqZone.summary);
        }
        if (null != qqZone.imageUrlOrFilePath && qqZone.imageUrlOrFilePath.size() > 0) {
            String first = qqZone.imageUrlOrFilePath.get(0);
            boolean isNetImages = false;
            if (!TextUtils.isEmpty(first) && first.startsWith("http")) {
                isNetImages = true;
            }
            if (isNetImages) {
                params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, qqZone.imageUrlOrFilePath);
            } else {
                params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, qqZone.imageUrlOrFilePath);
            }
        } else {//qq空间分享必须带图了，这里来一个默认图
            ArrayList<String> list = new ArrayList<>();
            list.add(ShareConstant.SHARE_DEFAULT_IMAGE);
            params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, list);
        }
        if (!TextUtils.isEmpty(qqZone.site)) {
            params.putString(QzoneShare.SHARE_TO_QQ_SITE, qqZone.site);
        }

        return params;
    }

    //发送分享结果
    private void postShareSuccess(final ShareData shareData) {
        if (null == shareData) {
            return;
        }
        final ShareData.OnShareListener callback = shareData.mShareListener;
        if (null == callback) {
            return;
        }
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess();
                shareData.mShareListener = null;
            }
        });
    }

    private void postShareCancel(final ShareData shareData) {
        if (null == shareData) {
            return;
        }
        final ShareData.OnShareListener callback = shareData.mShareListener;
        if (null == callback) {
            return;
        }
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(new Runnable() {
            @Override
            public void run() {
                callback.onCancel();
                shareData.mShareListener = null;
            }
        });
    }

    private void postShareError(final ShareData shareData) {
        if (null == shareData) {
            return;
        }
        final ShareData.OnShareListener callback = shareData.mShareListener;
        if (null == callback) {
            return;
        }
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(new Runnable() {
            @Override
            public void run() {
                callback.onFail();
                shareData.mShareListener = null;
            }
        });
    }

    //分享监听
    private class TencentShareListener implements IUiListener {
        private Activity activity;
        private Tencent tencent;
        private final ShareData shareData;

        private TencentShareListener(ShareData shareData) {
            this.shareData = shareData;
        }

        @Override
        public void onComplete(Object o) {
            if (isRelease) {
                return;
            }
            JSONObject jsonObject = (JSONObject) o;
            if (jsonObject != null) {
                String ret = jsonObject.optString("ret");
                String msg = jsonObject.optString("msg");
                if (ret.equals("0")) {
                    NullableToast.showSysToast("分享成功");
                    postShareSuccess(shareData);
                } else if (msg.equals("token is invalid")) {//access token废除。token被回收，或者被用户删除。请重新走登录流程{"ret":-23,"msg":"token is invalid"}
                    AccessTokenUtils.clear(activity, AccessTokenUtils.TENCENT_QZONE_PREFERENCES);
                    tencent.login(activity, ShareConstant.QZONE_SCOPE, new TencentLoginListener(TencentLoginListener.SOURCE_SHARE, shareData));
                } else if (ret.endsWith("100030")) {
                    NullableToast.showSysToast(ret + "没有分享权限，请重新授权");
                    AccessTokenUtils.clear(mActivity.getApplicationContext(), AccessTokenUtils.TENCENT_QZONE_PREFERENCES);
                    tencent.login(activity, ShareConstant.QZONE_SCOPE, new TencentLoginListener(TencentLoginListener.SOURCE_SHARE, shareData));
                } else {
                    //发送失败，清空授权信息
                    NullableToast.showSysToast(ret + "发送失败,请确认授权发送分享");
                    AccessTokenUtils.clear(activity, AccessTokenUtils.TENCENT_QZONE_PREFERENCES);
                }
            }
        }

        @Override
        public void onError(UiError uiError) {
            if (isRelease) {
                return;
            }
            NullableToast.showSysToast("与QQ通讯失败");
            postShareError(shareData);
        }

        @Override
        public void onCancel() {
            if (isRelease) {
                return;
            }
            NullableToast.showSysToast("发送取消");
            postShareCancel(shareData);
        }

        @Override
        public void onWarning(int i) {

        }
    }//

    //qq登录监听
    private class TencentLoginListener implements IUiListener {
        final static int SOURCE_SHARE = 2;

        private int source = 1;
        private final ShareData shareData;

        TencentLoginListener(int source, ShareData shareData) {
            this.source = source;
            this.shareData = shareData;
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onComplete(Object data) {
            if (isRelease) {
                return;
            }
            JSONObject jdata = (JSONObject) data;
            try {
                int ret = jdata.getInt("ret");
                String accessToken = jdata.optString("access_token", "");
                String openid = jdata.optString("openid", "");
                String expiresIn = jdata.optString("expires_in", "");
                QzoneOAuthV2 auth = AccessTokenUtils.doReadTencentQzoneToken(mActivity.getApplicationContext());
                auth.accessToken = accessToken;
                auth.openId = openid;
                auth.expiresIn = expiresIn;
                AccessTokenUtils.doSaveTencentQzoneToken(mActivity, auth);
                NullableToast.showSysToast("认证成功");
                //认证完毕，再次调用分享
                share(shareData);
            } catch (Exception e) {
                e.printStackTrace();
                NullableToast.showSysToast("授权失败");
            }
            Context context = mActivity.getApplicationContext();
            if (context != null) {
                Tencent mQQAuth = Tencent.createInstance(ShareConstant.QQ_APP_ID, context);
                com.tencent.connect.UserInfo mInfo = new com.tencent.connect.UserInfo(mActivity.getApplicationContext(), mQQAuth.getQQToken());
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
                            AccessTokenUtils.doSaveUserInfoByType(mActivity, null, name, AccessTokenUtils.SOURCE_QZONE);
                        }
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onWarning(int i) {
                    }
                });
            }
        }

        @Override
        public void onError(UiError arg0) {
            if (isRelease) {
                return;
            }
            NullableToast.showSysToast("授权失败");
        }

        @Override
        public void onWarning(int i) {

        }
    }//
}
