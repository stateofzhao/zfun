package com.zfun.sharelib;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.openapi.IWBAPI;
import com.sina.weibo.sdk.openapi.WBAPIFactory;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.Tencent;
import com.zfun.sharelib.core.ShareConstant;
import com.zfun.sharelib.init.InternalShareInitBridge;

public class SdkApiProvider {
    public final static int SINA_REQUEST_CODE = 32973;

    private static Tencent mTencent = null;//qq
    private static IWXAPI mWXAPI = null;//wx
    private static IWBAPI mWBAPI = null;//sinaweibo

    @NonNull
    public synchronized static IWXAPI getWXAPI(@NonNull Context context) {
        if (null == mWXAPI) {
            mWXAPI = WXAPIFactory.createWXAPI(context.getApplicationContext(), ShareConstant.WX_APP_ID, true, ConstantsAPI.LaunchApplication.LAUNCH_MODE_USING_START_ACTIVITY);
            mWXAPI.registerApp(ShareConstant.WX_APP_ID);
        }
        return mWXAPI;
    }

    @NonNull
    public synchronized static Tencent getTencentAPI(@NonNull Context context) {
        if (null == mTencent) {
            mTencent = Tencent.createInstance(
                    ShareConstant.QQ_APP_ID,
                    context.getApplicationContext(),
                    // 第三个参数是清单中注册的FileProvider的authorities属性
                    InternalShareInitBridge.getInstance().getInitParams().getFileProviderAuthorities());
            Tencent.setIsPermissionGranted(true);
        }
        if (null == mTencent) {
            InternalShareInitBridge.getInstance().getDebugCheck().classicAssert(false, new Throwable("QQ分享SDK初始化时失败"));
        }
        return mTencent;
    }

    @NonNull
    public synchronized static IWBAPI getWBAPI(@NonNull Context context){
        if (mWBAPI == null) {
            try {
                mWBAPI = WBAPIFactory.createWBAPI(InternalShareInitBridge.getInstance().getApplicationContext());
                mWBAPI.registerApp(context.getApplicationContext(), new AuthInfo(context.getApplicationContext(), ShareConstant.SINA_APP_KEY, ShareConstant.SINA_REDIRECT_URL, ShareConstant.SINA_SCOPE));
            } catch (Throwable e) {
                InternalShareInitBridge.getInstance().getDebugCheck().classicAssert(false, e);
            }
        }
        return mWBAPI;
    }
}
