package com.zfun.sharelib;

import android.content.Context;

import com.zfun.sharelib.core.ShareConstant;
import com.zfun.sharelib.init.InternalShareInitBridge;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.openapi.IWBAPI;
import com.sina.weibo.sdk.openapi.WBAPIFactory;
import com.tencent.tauth.Tencent;


/**
 * Seriously, you should say something about your code
 * Author: hongze
 * Date: 13-9-30
 * Time: 下午3:56
 */
public class SsoFactory {
    public final static int SINA_REQUEST_CODE = 32973;

    private static Tencent mTencent = null;
    private static IWBAPI mWBAPI = null;

    public static Tencent getTencentInstance() {
        if (mTencent == null) {
            synchronized (SsoFactory.class) {
                try {
                    if(null == mTencent){
                        if(InternalShareInitBridge.getInstance().getHostActivity().getApplicationContext() !=null){
                            mTencent = Tencent.createInstance(ShareConstant.QQ_APP_ID,
                                    InternalShareInitBridge.getInstance().getHostActivity().getApplicationContext());
                        }
                    }
                } catch (Throwable e) {
                    InternalShareInitBridge.getInstance().getDebugCheck().classicAssert(false, e);
                }
            }
        }
        return mTencent;
    }

    public static IWBAPI getWBAPI() {
        return getWBAPI(InternalShareInitBridge.getInstance().getHostActivity());
    }

    public static IWBAPI getWBAPI(Context context) {
        if (!InternalShareInitBridge.getInstance().isPrivacyPolicyAgreed()) {
            return null;
        }
        if (mWBAPI == null) {
            try {
                mWBAPI = WBAPIFactory.createWBAPI(context);
                mWBAPI.registerApp(context, new AuthInfo(context, ShareConstant.SINA_APP_KEY, ShareConstant.SINA_REDIRECT_URL, ShareConstant.SINA_SCOPE));
//                mSsoHandler = new SsoHandler(activity);
            } catch (Throwable e) {
                InternalShareInitBridge.getInstance().getDebugCheck().classicAssert(false, e);
            }
        }
        return mWBAPI;
    }

    public static IWBAPI getAloneWBAPI(Context context){
        if (!InternalShareInitBridge.getInstance().isPrivacyPolicyAgreed()) {
            return null;
        }
        IWBAPI result = null;
        try {
            result = WBAPIFactory.createWBAPI(context);
            result.registerApp(context, new AuthInfo(context, ShareConstant.SINA_APP_KEY, ShareConstant.SINA_REDIRECT_URL, ShareConstant.SINA_SCOPE));
        } catch (Throwable e) {
            InternalShareInitBridge.getInstance().getDebugCheck().classicAssert(false, e);
        }
        return result;
    }

    /**
     * IWBAPI创建时会保存传入的Activity，如果activity被销毁了就会无法拉起微博授权
     * 所以在其他Activity中使用WBAPI时，在onDestroy中需要把WBAPI对象销毁，再次调用时再新建，否则会有bug
     * 沙雕微博，不干人事
     */
    private static IWBAPI loginBridgeWBAPI = null;
    public static IWBAPI getKugouLoginWBAPI(Context activity) {
        if (!InternalShareInitBridge.getInstance().isPrivacyPolicyAgreed()) {
            return null;
        }
        if (loginBridgeWBAPI == null) {
            try {
                loginBridgeWBAPI = WBAPIFactory.createWBAPI(activity);
                loginBridgeWBAPI.registerApp(activity, new AuthInfo(activity, ShareConstant.SINA_APP_KEY, ShareConstant.SINA_REDIRECT_URL, ShareConstant.SINA_SCOPE));
            } catch (Throwable e) {
                InternalShareInitBridge.getInstance().getDebugCheck().classicAssert(false, e);
            }
        }
        return loginBridgeWBAPI;
    }

    public static void destroyKugouLoginWBAPI() {
        loginBridgeWBAPI = null;
    }
}
