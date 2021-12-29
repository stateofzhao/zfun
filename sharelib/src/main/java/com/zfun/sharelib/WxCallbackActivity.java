package com.zfun.sharelib;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zfun.sharelib.core.ShareConstant;
import com.zfun.sharelib.init.InitContext;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/**
 * Created by lzf on 2021/12/22 2:47 下午
 */
public class WxCallbackActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI mIWXAPI;
    @Nullable
    private final Activity mOutWxEntryActivity;

    public WxCallbackActivity(){
        mOutWxEntryActivity = null;
    }

    public WxCallbackActivity(@NonNull Activity yourWxEntryActivity){
        mOutWxEntryActivity = yourWxEntryActivity;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIWXAPI = WXAPIFactory.createWXAPI(this, ShareConstant.WX_APP_ID, true);
        mIWXAPI.registerApp(ShareConstant.WX_APP_ID);
        mIWXAPI.handleIntent(getIntent(), this);
    }

    //微信打开酷我
    @Override
    public void onReq(BaseReq baseReq) {
        Activity realActivity = null == mOutWxEntryActivity?this:mOutWxEntryActivity;
        InitContext.getInstance().getOptWxCallback(realActivity).onOptWxReq(baseReq);
    }

    //调用微信api的回调，例如分享
    @Override
    public void onResp(BaseResp baseResp) {
        Activity realActivity = null == mOutWxEntryActivity?this:mOutWxEntryActivity;
        InitContext.getInstance().getOptWxCallback(realActivity).onOptWxResp(baseResp);
    }
}
