package com.zfun.sharelib.init;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zfun.sharelib.ShareMgrImpl;
import com.zfun.sharelib.core.IShareHandler;
import com.zfun.sharelib.core.ShareConstant;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX;
import com.zfun.sharelib.core.WeixinLoginHandler;

/**
 * Created by lzf on 2021/12/22 2:58 下午
 */
public class NullableOptWxCallback implements IOptWxCallback {
    private final @Nullable Class<?> appEntryActivity;
    private final Activity WxCallbackActivity;

    public NullableOptWxCallback(@NonNull Activity WxCallbackActivity, @Nullable Class<?> appEntryActivity) {
        this.WxCallbackActivity = WxCallbackActivity;
        this.appEntryActivity = appEntryActivity;
    }

    @Override
    public void onOptWxReq(BaseReq baseReq) {
        switch (baseReq.getType()) {
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                if(null != appEntryActivity){
                    ShowMessageFromWX.Req req = (ShowMessageFromWX.Req) baseReq;
                    Intent newIntent = new Intent(WxCallbackActivity, appEntryActivity);
                    if (req.message != null && !TextUtils.isEmpty(req.message.messageExt)) {
                        newIntent.setData(Uri.parse(req.message.messageExt));
                    }
                    WxCallbackActivity.startActivity(newIntent);
                }
                WxCallbackActivity.finish();
                break;
            // 可能有其他类型的消息，默认都把自己app起来，避免这个透明Activity卡住用户操作
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
            default:
                // 微信调用自己app的界面
                if(null != appEntryActivity){
                    Intent intent = new Intent(WxCallbackActivity, appEntryActivity);
                    WxCallbackActivity.startActivity(intent);
                }
                WxCallbackActivity.finish();
                break;
        }
    }

    @Override
    public void onOptWxResp(BaseResp baseResp) {
        String resultMsg = "";
        boolean isShare = isShareResp(baseResp);
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                if (baseResp instanceof SendAuth.Resp) {//授权
                    resultMsg = "授权成功";
                    final String state = ((SendAuth.Resp) baseResp).state;
                    final String accessCode = ((SendAuth.Resp) baseResp).code;
                    final WeixinLoginHandler handler = getWeixinLoginHandler();
                    if(null != handler){
                        handler.postSuc(accessCode,state);
                    }
                } else {
                    if (isShare) {
                        resultMsg = "分享成功";
                    }
                }
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                if (baseResp instanceof SendAuth.Resp) {//授权
                    resultMsg = "用户取消授权";
                    final WeixinLoginHandler handler = getWeixinLoginHandler();
                    if(null != handler){
                        handler.postCancel();
                    }
                } else {
                    if (isShare) {
                        resultMsg = "发送取消";
                    }
                }
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                if (baseResp instanceof SendAuth.Resp) {//授权
                    resultMsg = "用户拒绝授权";
                    final WeixinLoginHandler handler = getWeixinLoginHandler();
                    if(null != handler){
                        handler.postFail();
                    }
                } else {
                    if (isShare) {
                        resultMsg = "发送被拒绝";
                    }
                }
                break;
            default:
                if (baseResp instanceof SendAuth.Resp) {//授权
                    resultMsg = "授权失败";
                    final WeixinLoginHandler handler = getWeixinLoginHandler();
                    if(null != handler){
                        handler.postFail();
                    }
                } else {
                    if (isShare) {
                        resultMsg = "发送失败";
                    }
                }
                break;
        }
        WxCallbackActivity.finish();
        NullableToast.showDialogTip(resultMsg);
    }

    private boolean isShareResp(@NonNull BaseResp baseResp) {
        String trans = baseResp.transaction;
        return !TextUtils.isEmpty(trans) && trans.contains(ShareConstant.SHARE_TAG_STR);
    }

    @Nullable
    private WeixinLoginHandler getWeixinLoginHandler(){
        final IShareHandler currentHandler = ShareMgrImpl.getInstance().getCurShareHandler();
        if(currentHandler instanceof WeixinLoginHandler){
            return (WeixinLoginHandler) currentHandler;
        }
        return null;
    }
}
