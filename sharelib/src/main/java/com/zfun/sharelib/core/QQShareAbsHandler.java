package com.zfun.sharelib.core;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.zfun.sharelib.init.InternalShareInitBridge;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.Tencent;

/**
 * QQ有关的分享。
 * <p/>
 * Created by zfun on 2017/8/4.
 */
abstract class QQShareAbsHandler implements IShareHandler {
    protected Context mContext;
    protected boolean isRelease = true;

    /**
     * 获取图文分享参数
     *
     * @param targetUrl 必填，这条分享消息被好友点击后的跳转URL
     * @param title 必填，分享的标题, 最长30个字符
     * @param summary 可选，分享的消息摘要，最长40个字
     * @param imageUri 可选，分享图片的URL或者本地路径
     * @param appName 可选，手Q客户端顶部，替换“返回”按钮文字，如果为空，用返回代替
     * @param ext 可选，分享额外选项，两种类型可选（默认是不隐藏分享到QZone按钮且不自动打开分享到QZone的对话框）：
     * QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN，分享时自动打开分享到QZone的对话框。
     * QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE，分享时隐藏分享到QZone按钮
     */
    Bundle getImageTextParams(String targetUrl, String title, String summary, String imageUri,
            String appName, String site, int ext) {
        if (TextUtils.isEmpty(targetUrl) || TextUtils.isEmpty(title)) {
            throw new IllegalArgumentException("targetUrl or title must not null!");
        }

        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);

        if (!TextUtils.isEmpty(summary)) {
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        }
        if (!TextUtils.isEmpty(imageUri)) {
            if (imageUri.startsWith("http")) {
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUri);
            } else {
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageUri);
            }
        }
        if (!TextUtils.isEmpty(appName)) {
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, appName);
        }

        if (ext > 0) {
            params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, ext);
        }

        return params;
    }

    /**
     * 获取纯图片分享的参数
     *
     * @param imageURLorFilePath 必选，需要分享的图片本地路径或者URL
     * @param appName 可选，手Q客户端顶部，替换“返回”按钮文字，如果为空，用返回代替
     * @param ext 可选，分享额外选项，两种类型可选（默认是不隐藏分享到QZone按钮且不自动打开分享到QZone的对话框）：
     * QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN，分享时自动打开分享到QZone的对话框。
     * QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE，分享时隐藏分享到QZone按钮
     */
    Bundle getImageParams(String imageURLorFilePath, String appName, int ext) {
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageURLorFilePath);

        if (!TextUtils.isEmpty(appName)) {
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, appName);
        }
        if (ext > 0) {
            params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, ext);
        }

        return params;
    }

    /**
     * 获取音乐分享的参数
     *
     * @param targetUrl 必选，这条分享消息被好友点击后的跳转URL。
     * @param audioUrl 必选，音乐文件的远程链接, 以URL的形式传入, 不支持本地音乐。
     * @param summary 可选，分享的消息摘要，最长40个字
     * @param imageUri 可选，分享图片的URL或者本地路径
     * @param appName 可选，手Q客户端顶部，替换“返回”按钮文字，如果为空，用返回代替
     * @param ext 可选，分享额外选项，两种类型可选（默认是不隐藏分享到QZone按钮且不自动打开分享到QZone的对话框）：
     * QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN，分享时自动打开分享到QZone的对话框。
     * QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE，分享时隐藏分享到QZone按钮
     */
    Bundle getMusicParams(String targetUrl, String audioUrl, String title, String summary,
            String imageUri, String appName, int ext) {
        final Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);
        params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, audioUrl);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, title);

        if (!TextUtils.isEmpty(summary)) {
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary);
        }
        if (!TextUtils.isEmpty(imageUri)) {
            if (imageUri.startsWith("http")) {
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUri);
            } else {
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageUri);
            }
        }
        if (!TextUtils.isEmpty(appName)) {
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, appName);
        }

        if (ext > 0) {
            params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, ext);
        }

        return params;
    }

    /**
     * 获取应用分享的参数
     */
    Bundle getAppShareParams() {
        return null;
    }

    public Tencent createTencent(@NonNull Context applicationContext) {
        return Tencent.createInstance(ShareConstant.QQ_APP_ID, applicationContext.getApplicationContext());
    }

    @Override
    public boolean isSupport() {
        return true;
    }

    @Override
    public void init() {
        mContext = InternalShareInitBridge.getInstance().getApplicationContext();
        isRelease = false;
    }

    @Override
    public void release() {
        isRelease = true;
        mContext = null;
    }
}
