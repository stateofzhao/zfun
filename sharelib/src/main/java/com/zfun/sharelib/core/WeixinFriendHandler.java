package com.zfun.sharelib.core;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;

import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.init.NullableToast;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;

/**
 * 微信好友分享
 * <p/>
 * Created by zfun on 2017/8/8 16:15
 */
public class WeixinFriendHandler extends WeixinAbsShareHandler {

    @Override
    public void share(@NonNull ShareData shareData) {
        if(isRelease){
            return;
        }
        final Activity compelActivity = shareData.getCompelContext();
        if (null == mContext && null == compelActivity) {
            return;
        }
        final Context realContext = null != compelActivity ? compelActivity : mContext;
        final IWXAPI api = ShareMgrImpl.getInstance().getWxApi();
        if(null == api){
            return;
        }
        if (api.isWXAppInstalled()) {
            if (api.getWXAppSupportAPI() >= SESSION_SUPPORTED_VERSION) {
                doShare(shareData, api);
            } else {
                NullableToast.showDialogTip(realContext, "update");
            }
        } else {
            NullableToast.showDialogTip(realContext, "install");
        }
    }

    @Override
    public boolean isSupport() {
        return true;
    }

    @Override
    int scene() {
        return SendMessageToWX.Req.WXSceneSession;
    }
}
