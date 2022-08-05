package com.zfun.sharelib.core;

import androidx.annotation.NonNull;

import com.zfun.sharelib.SdkApiProvider;
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
        if (null == mContext) {
            return;
        }
        final IWXAPI api = SdkApiProvider.getWXAPI(mContext);
        if (api.isWXAppInstalled()) {
            if (api.getWXAppSupportAPI() >= SESSION_SUPPORTED_VERSION) {
                doShare(shareData, api);
            } else {
                postShareError();
                NullableToast.showDialogTip( "微信版本过低");
            }
        } else {
            postShareError();
            NullableToast.showDialogTip("微信未安装");
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
