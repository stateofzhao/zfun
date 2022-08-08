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

import java.lang.ref.WeakReference;

/**
 * Created by lzf on 2021/12/22 2:58 下午
 */
public class NullableOptWxCallback implements IOptWxCallback {
    private final Class<?> desActivity;
    private final Activity WxCallbackActivity;

    private static WeakReference<IOptWxCallback> reference;

    public NullableOptWxCallback(@NonNull Activity WxCallbackActivity, @NonNull Class<?> desActivity) {
        this.WxCallbackActivity = WxCallbackActivity;
        this.desActivity = desActivity;
    }

    public static synchronized IOptWxCallback get(Activity wxCallbackActivity, Class<?> appEntryActivity) {
        if (null == reference || null == reference.get()) {
            reference = new WeakReference<>(new NullableOptWxCallback(wxCallbackActivity, appEntryActivity));
        }
        return reference.get();
    }

    @Override
    public void onOptWxReq(BaseReq baseReq) {
        switch (baseReq.getType()) {
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                ShowMessageFromWX.Req req = (ShowMessageFromWX.Req) baseReq;
                Intent newIntent = new Intent(WxCallbackActivity, desActivity);
                if (req.message != null && !TextUtils.isEmpty(req.message.messageExt)) {
                    newIntent.setData(Uri.parse(req.message.messageExt));
                }
                WxCallbackActivity.startActivity(newIntent);
                WxCallbackActivity.finish();
                break;
            // 可能有其他类型的消息，默认都把自己app起来，避免这个透明Activity卡住用户操作
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
            default:
                // 微信调用自己app的界面
                Intent intent = new Intent(WxCallbackActivity, desActivity);
                WxCallbackActivity.startActivity(intent);
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
