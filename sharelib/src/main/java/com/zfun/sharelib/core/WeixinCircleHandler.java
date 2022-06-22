package com.zfun.sharelib.core;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;

import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.init.NullableToast;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.openapi.IWXAPI;

/**
 * 处理微信朋友圈分享
 * <p/>
 * Created by lizhaofei on 2017/8/8 13:56
 */
public class WeixinCircleHandler extends WeixinAbsShareHandler {

    //回调在{@link WXEntryActivity}中
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
        if (api != null && api.isWXAppInstalled()) {
            if (api.getWXAppSupportAPI() >= TIMELINE_SUPPORTED_VERSION) {
               /* smallImage(shareData);*/
                doShare(shareData, api);
            } else {
                NullableToast.showDialogTip(realContext,"微信版本过低");
            }
        } else {
            NullableToast.showDialogTip(realContext, "微信未安装");
        }
    }

    @Override
    public boolean isSupport() {
        return true;
    }

    @Override
    int scene() {
        return SendMessageToWX.Req.WXSceneTimeline;
    }

   /* private void smallImage(@NonNull ShareData shareData){
        final ShareData.Wx wx = shareData.getWxShareData();
        wx.thumbData = ShareUtils.imgThumbFromByte(mContext,wx.thumbData, ShareConstant.IMAGE_THUMB_SIZE,
                ShareConstant.IMAGE_THUMB_SIZE,
                ShareConstant.WX_SMALL_APP_THUMB_MAX_STORAGE_SIZE);
    }*/
}
