package com.zfun.sharelib.core;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;

import com.tencent.connect.share.QzonePublish;
import com.zfun.sharelib.SdkApiProvider;
import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.init.InternalShareInitBridge;
import com.zfun.sharelib.init.NullableToast;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import java.util.ArrayList;


/**
 * QQ空间分享
 * <p/>
 * Created by zfun on 2017/8/4.
 */
//这个应该有两种形式，一种是需要打开一个新的Fragment来让用户编辑 简介；一种是直接发送到QQ中。
public class QQZoneShareHandler extends QQShareAbsHandler {
    private static final String TAG = "QQZoneShareHandler";
    private TencentShareListener listener;

    @Override
    public void share(@NonNull final ShareData shareData) {
        if (isRelease) {//防止 由于异步任务执行完毕后，回调这里，但是ShareMgrImpl已经被relase掉了，而出现的NullPointException
            return;
        }
        if (null == mContext) {
            return;
        }
        final Activity activity = shareData.getQQZoneShareData().activityRef.get();
        if(null == activity){
            return;
        }
        final Tencent tencent = SdkApiProvider.getTencentAPI(mContext);
        Bundle params = getShareParams(shareData);
        if (null == params) {
            postShareError(shareData,"分享失败-无法获取分享参数");
            return;
        }
        listener =  new TencentShareListener(shareData);
        tencent.shareToQzone(activity, params, listener);
    }

    @Nullable
    @Override
    public IUiListener getUiListener() {
        return listener;
    }

    private Bundle getShareParams(ShareData shareData) {
        ShareData.QQZone qqZone = shareData.getQQZoneShareData();
        if (null == qqZone) {
            return null;
        }
        final int type = qqZone.shareType;

        Bundle params = new Bundle();
        if (type == ShareData.QQZone.TYPE_IMAGE_TEXT){
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);//qq空间
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
        } else if (type == ShareData.QQZone.TYPE_MOOD){
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY,qqZone.summary);
            params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL,qqZone.imageUrlOrFilePath);
            Bundle extParas = new Bundle();
            extParas.putString(QzonePublish.HULIAN_CALL_BACK,qqZone.hulianCallBack);
            extParas.putString(QzonePublish.HULIAN_EXTRA_SCENE,qqZone.hulianScene);
            params.putBundle(QzonePublish.PUBLISH_TO_QZONE_EXTMAP,extParas);
        } else if (type == ShareData.QQZone.TYPE_PUBLISH_VIDEO){
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHVIDEO);
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY,qqZone.summary);
            params.putString(QzonePublish.PUBLISH_TO_QZONE_VIDEO_PATH,qqZone.videoLocalPath);
            Bundle extParas = new Bundle();
            extParas.putString(QzonePublish.HULIAN_CALL_BACK,qqZone.hulianCallBack);
            extParas.putString(QzonePublish.HULIAN_EXTRA_SCENE,qqZone.hulianScene);
            params.putBundle(QzonePublish.PUBLISH_TO_QZONE_EXTMAP,extParas);
        }

        if (!TextUtils.isEmpty(qqZone.site)) {
            params.putString(QzoneShare.SHARE_TO_QQ_SITE, qqZone.site);
        }

        return params;
    }

    //发送分享结果
    private void postShareSuccess(final ShareData shareData,final String msg) {
        if (!TextUtils.isEmpty(msg)){
            NullableToast.showSysToast(msg);
        }
        if (null == shareData) {
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        final ShareData.OnShareListener callback = shareData.mShareListener;
        if (null == callback) {
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(() -> {
            callback.onSuccess(msg);
            shareData.mShareListener = null;
            ShareMgrImpl.getInstance().clearCurShareHandler();
        });
    }

    private void postShareCancel(final ShareData shareData,final String msg) {
        if (!TextUtils.isEmpty(msg)){
            NullableToast.showSysToast(msg);
        }
        final ShareData.OnShareListener callback = shareData.mShareListener;
        if (null == callback) {
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(() -> {
            callback.onCancel(msg);
            shareData.mShareListener = null;
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
        final ShareData.OnShareListener callback = shareData.mShareListener;
        if (null == callback) {
            ShareMgrImpl.getInstance().clearCurShareHandler();
            return;
        }
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(() -> {
            callback.onFail(msg);
            shareData.mShareListener = null;
            ShareMgrImpl.getInstance().clearCurShareHandler();
        });
    }

    //分享监听
    private class TencentShareListener implements IUiListener {
        private final ShareData shareData;

        private TencentShareListener(ShareData shareData) {
            this.shareData = shareData;
        }

        @Override
        public void onComplete(Object o) {
            listener = null;
            if (isRelease) {
                return;
            }
            JSONObject jsonObject = (JSONObject) o;
            if (jsonObject != null) {
                String ret = jsonObject.optString("ret");
                String msg = jsonObject.optString("msg");
                if (ret.equals("0")) {
                    postShareSuccess(shareData,"分享成功");
                }
                /*else if (msg.equals("token is invalid")) {//access token废除。token被回收，或者被用户删除。请重新走登录流程{"ret":-23,"msg":"token is invalid"}
                    AccessTokenUtils.clear(activity, AccessTokenUtils.TENCENT_QZONE_PREFERENCES);
                    tencent.login(activity, ShareConstant.QZONE_SCOPE, new TencentLoginListener(TencentLoginListener.SOURCE_SHARE, shareData));
                } else if (ret.endsWith("100030")) {
                    NullableToast.showSysToast(ret + "没有分享权限，请重新授权");
                    AccessTokenUtils.clear(mContext.getApplicationContext(), AccessTokenUtils.TENCENT_QZONE_PREFERENCES);
                    tencent.login(activity, ShareConstant.QZONE_SCOPE, new TencentLoginListener(TencentLoginListener.SOURCE_SHARE, shareData));
                }*/
                else {
                    //发送失败，清空授权信息
                    NullableToast.showSysToast(ret + "发送失败："+msg);
                    /*AccessTokenUtils.clear(activity, AccessTokenUtils.TENCENT_QZONE_PREFERENCES);*/
                }
            }
        }

        @Override
        public void onError(UiError uiError) {
            listener = null;
            if (isRelease) {
                return;
            }
            postShareError(shareData,"分享失败，请稍后重试");
        }

        @Override
        public void onCancel() {
            listener = null;
            if (isRelease) {
                return;
            }
            postShareCancel(shareData,"取消分享");
        }

        @Override
        public void onWarning(int i) {
            listener = null;
        }
    }//

}
