package com.zfun.sharelib.init;


/**
 * Created by lzf on 2021/12/21 5:27 下午
 */
public class NullableToast {
    public static void showSysToast(String msg){
        final IToast toast= InternalShareInitBridge.getInstance().getTipToast();
        if(null != toast){
            toast.showTip(msg);
        }
    }

    public static void showDialogTip(String msg){
        final IToast toast= InternalShareInitBridge.getInstance().getTipToast();
        if(null != toast){
            toast.showDialogTip(msg);
        }
    }
}
