package com.zfun.sharelib.core;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import androidx.annotation.NonNull;

import android.os.Build;
import android.text.TextUtils;

import com.zfun.sharelib.init.InternalShareInitBridge;
import com.zfun.sharelib.init.NullableToast;

/**
 * 复制链接
 * <p/>
 * Created by lizhaofei on 2017/8/8 17:44
 */
public class CopyUrlHandler implements IShareHandler {
    private Context mContext;
    private boolean isRelease = true;

    @Override
    public void share(@NonNull ShareData shareData) {
        if (null == mContext || isRelease) {
            return;
        }

        String url = shareData.getCopyUrl();
        if (TextUtils.isEmpty(url)) {//为空的话取 酷我移动首页 url
            url = ShareConstant.SHARE_DEFAULT_COPY_URL;
        }

        ClipboardManager copy = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        if(null == copy){
            return;
        }
        if (hasHoneycomb()) {
            ClipData cData = ClipData.newPlainText("info", url);
            copy.setPrimaryClip(cData);
        } else {
            copy.setText(url);
        }
        NullableToast.showSysToast("链接复制成功");
    }

    @Override
    public boolean isSupport() {
        return null != mContext;
    }

    @Override
    public void init() {
        isRelease = false;
        mContext = InternalShareInitBridge.getInstance().getHostActivity();
    }

    @Override
    public void release() {
        isRelease = true;
        mContext = null;
    }

    private boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
}
