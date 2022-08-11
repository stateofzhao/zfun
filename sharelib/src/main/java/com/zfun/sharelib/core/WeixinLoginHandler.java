package com.zfun.sharelib.core;

import androidx.annotation.NonNull;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.zfun.sharelib.SdkApiProvider;
import com.zfun.sharelib.init.InternalShareInitBridge;
import com.zfun.sharelib.init.NullableToast;

public class WeixinLoginHandler implements IShareHandler {
    private ShareData mShareData;

    @Override
    public void share(@NonNull ShareData shareData) {
        mShareData = shareData;
        final int type = shareData.getWxShareData().type;
        if (ShareData.Wx.TYPE_LOGIN != type) {
            postCancel();
            return;
        }
        final IWXAPI iwxapi = SdkApiProvider.getWXAPI(InternalShareInitBridge.getInstance().getApplicationContext());
        if (!iwxapi.isWXAppInstalled()) {
            postFail();
            NullableToast.showDialogTip("微信未安装");
            return;
        }
        final SendAuth.Req authReq = new SendAuth.Req();
        authReq.scope = "snsapi_userinfo";
        authReq.state = getWXLoginState(new Object());
        final boolean sendOK = iwxapi.sendReq(authReq);
        if(!sendOK){
            postFail();
        }
    }

    @Override
    public boolean isSupport() {
        return true;
    }

    @Override
    public void init() {

    }

    @Override
    public void release() {
        end2Clear();
    }

    public void postCancel() {
        final ShareData.OnWXLoginListener listener = null!=mShareData&&null!=mShareData.mShareListener?mShareData.mWXLoginListener:null;
        if (null == listener) {
            return;
        }
        end2Clear();
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(listener::onCancel);
    }

    public void postFail() {
        final ShareData.OnWXLoginListener listener = null!=mShareData&&null!=mShareData.mShareListener?mShareData.mWXLoginListener:null;
        if (null == listener) {
            return;
        }
        end2Clear();
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(listener::onFail);
    }

    public void postSuc(final String code,final String state){
        final ShareData.OnWXLoginListener listener = null!=mShareData&&null!=mShareData.mWXLoginListener?mShareData.mWXLoginListener:null;
        if (null == listener) {
            return;
        }
        final boolean isStateOk = isWXLoginStateOk(state);
        if(!isStateOk){
            InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(listener::onFail);
            return;
        }
        end2Clear();
        InternalShareInitBridge.getInstance().getMessageHandler().asyncRunInMainThread(()->{
            listener.onSuc(code,state);
        });
    }

    private void end2Clear(){
        mShareData = null;
        clearWxLoginStateFlag();
    }

    private static Object wxLoginStateFlag;

    public static String getWXLoginState(@NonNull Object object) {
        wxLoginStateFlag = object;
        return ShareConstant.WX_LOGIN_STATE_PRE + "_" + object.hashCode();
    }

    public static boolean isWXLoginStateOk(String wxLoginState){
        if(null == wxLoginState || null == wxLoginStateFlag){
            return false;
        }
        return wxLoginState.equals(ShareConstant.WX_LOGIN_STATE_PRE + "_" + wxLoginStateFlag.hashCode());
    }

    public static void clearWxLoginStateFlag(){
        wxLoginStateFlag = null;
    }
}
