package com.zfun.sharelib.core;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.text.TextUtils;

import com.zfun.sharelib.SdkApiProvider;
import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.init.InternalShareInitBridge;
import com.zfun.sharelib.init.NullableToast;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * QQ好友分享
 * <p/>
 * Created by zfun on 2017/8/8 11:40
 */
public class QQFriendShareHandler extends QQShareAbsHandler {
    private final static String TAG = "QQFrendShareHandler";
    private QShareUiListener listener;

    @Override
    public void share(@NonNull ShareData shareData) {
        if (isRelease) {//防止 由于异步任务执行完毕后，回调这里，但是ShareMgrImpl已经被relase掉了，而出现的NullPointException
            return;
        }
        if (null == mContext) {
            return;
        }
        final Activity activity = shareData.getQQFriendData().activityRef.get();
        if(null == activity){
            return;
        }
        final Tencent tencent = SdkApiProvider.getTencentAPI(mContext);

        final ShareData.QQ qFrend = shareData.getQQFriendData();
        final int shareDataType = qFrend.shareType;

        Bundle params = null;
        if (shareDataType == QQShare.SHARE_TO_QQ_TYPE_DEFAULT) {//图文分享
            params = getImageTextParams(qFrend.targetUrl, qFrend.title, qFrend.summary,
                    qFrend.imageURLorFilePath, qFrend.appName, qFrend.site, qFrend.ext);
        } else if (shareDataType == QQShare.SHARE_TO_QQ_TYPE_AUDIO) {//音乐分享
            params = getMusicParams(qFrend.targetUrl, qFrend.audioUrl, qFrend.title, qFrend.summary,
                    qFrend.imageURLorFilePath, qFrend.appName, qFrend.ext);
        } else if (shareDataType == QQShare.SHARE_TO_QQ_TYPE_IMAGE) {//纯图片分享
            params = getImageParams(qFrend.imageURLorFilePath, qFrend.appName, qFrend.ext);
        }
        if (null == params) {
            return;
        }
        listener = new QShareUiListener(shareData);
        tencent.shareToQQ(activity, params, listener);
    }

    @Nullable
    @Override
    public IUiListener getUiListener() {
        return listener;
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
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(msg);
                shareData.mShareListener = null;
                ShareMgrImpl.getInstance().clearCurShareHandler();
            }
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

    private class QShareUiListener implements IUiListener {
        private final ShareData shareData;

        private QShareUiListener(ShareData shareData) {
            this.shareData = shareData;
        }

        @Override
        public void onComplete(Object jsonOb) {
            listener = null;
            if (isRelease) {
                return;
            }
            JSONObject jsonObject = (JSONObject) jsonOb;
            try {
                int ret = jsonObject.getInt("ret");
                if (ret == 0) {
                    postShareSuccess(shareData,"分享成功");
                } else {
                    postShareError(shareData,"分享失败-" + ret);
                }
            } catch (JSONException e) {
                postShareError(shareData,"分享失败");
            }
        }

        @Override
        public void onError(UiError uiError) {
            listener = null;
            if (isRelease) {
                return;
            }
            /*if (!isPkgInstalled("com.tencent.mobileqq")) {
                NullableToast.showSysToast("您没有安装QQ，暂时无法分享");
                postShareError(shareData);
            } else {
                NullableToast.showSysToast("分享出错，请稍后再试");
                postShareError(shareData);
            }*/
            postShareError(shareData,"分享出错，请稍后再试");
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
    }

    /**
     * 判断应用是否存在
     *
     * @param packageName
     * @return
     */
    public boolean isPkgInstalled(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        ApplicationInfo info = null;
        try {
            info = mContext.getPackageManager().getApplicationInfo(packageName, 0);
            return info != null;
        } catch (Exception e) {
            return false;
        }
    }
}
