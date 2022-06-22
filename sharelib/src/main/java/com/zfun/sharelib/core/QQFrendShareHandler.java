package com.zfun.sharelib.core;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.text.TextUtils;

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

/**
 * QQ好友分享
 * <p/>
 * Created by lizhaofei on 2017/8/8 11:40
 */
public class QQFrendShareHandler extends QQShareAbsHandler {
    private final static String TAG = "QQFrendShareHandler";

    @Override
    public void share(@NonNull ShareData shareData) {
        if (isRelease) {//防止 由于异步任务执行完毕后，回调这里，但是ShareMgrImpl已经被relase掉了，而出现的NullPointException
            return;
        }
        final Activity compelActivity = shareData.getCompelContext();
        if (null == mActivity && null == compelActivity) {
            return;
        }
        final Tencent tencent = ShareMgrImpl.getInstance().getTencent();
        final Activity realActivity = null != compelActivity ? compelActivity : mActivity;

        final ShareData.QQ qFrend = shareData.getQqFrendData();
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

        tencent.shareToQQ(realActivity, params, new QShareUiListener(shareData));
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

    private class QShareUiListener implements IUiListener {
        private final ShareData shareData;

        private QShareUiListener(ShareData shareData) {
            this.shareData = shareData;
        }

        @Override
        public void onComplete(Object jsonOb) {
            if (isRelease) {
                return;
            }
            JSONObject jsonObject = (JSONObject) jsonOb;
            try {
                int ret = jsonObject.getInt("ret");
                if (ret == 0) {
                    NullableToast.showSysToast("分享成功");
                    postShareSuccess(shareData);
                } else {
                    NullableToast.showSysToast("分享失败-" + ret);
                    postShareError(shareData);
                }
            } catch (JSONException e) {
                NullableToast.showSysToast("分享失败");
                postShareError(shareData);
            }
        }

        @Override
        public void onError(UiError uiError) {
            if (isRelease) {
                return;
            }
            if (!isPkgInstalled("com.tencent.mobileqq")) {
                NullableToast.showSysToast("您没有安装QQ，暂时无法分享");
                postShareError(shareData);
            } else {
                NullableToast.showSysToast("分享出错，请稍后再试");
                postShareError(shareData);
            }
        }

        @Override
        public void onCancel() {
            if (isRelease) {
                return;
            }
            NullableToast.showSysToast("取消分享");
            postShareCancel(shareData);
        }

        @Override
        public void onWarning(int i) {

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
            info = mActivity.getPackageManager().getApplicationInfo(packageName, 0);
            return info != null;
        } catch (Exception e) {
            return false;
        }
    }
}
