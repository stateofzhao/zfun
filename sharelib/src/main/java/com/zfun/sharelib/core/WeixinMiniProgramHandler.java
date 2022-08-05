package com.zfun.sharelib.core;

import com.zfun.sharelib.SdkApiProvider;
import com.zfun.sharelib.ShareUtils;
import com.zfun.sharelib.init.NullableToast;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import androidx.annotation.NonNull;

/**
 * 微信小程序
 * <p/>
 * Created by zfun on 2021/2/22 15:30
 */
public class WeixinMiniProgramHandler extends WeixinAbsShareHandler{
    @Override
    int scene() {
        return SendMessageToWX.Req.WXSceneSession; // 目前只支持会话
    }

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
            if (isSupport()) {
                /*smallImage(shareData);*/
                doShare(shareData, api);
            } else {
                NullableToast.showDialogTip("微信版本过低");
            }
        } else {
            NullableToast.showDialogTip("微信未安装");
        }
    }

    @Override
    public boolean isSupport() {
        return ShareUtils.isSupportSmallAppShare(mContext);
    }

   /* private void smallImage(@NonNull ShareData shareData){
        final ShareData.Wx wx = shareData.getWxShareData();
        wx.thumbData = ShareUtils.imgThumbFromByte(mContext,wx.thumbData, ShareConstant.WX_SMALL_APP_THUMB_IMAGE_SIZE,
                ShareConstant.WX_SMALL_APP_THUMB_IMAGE_SIZE,
                ShareConstant.WX_SMALL_APP_THUMB_MAX_STORAGE_SIZE);
    }*/
}
