package com.zfun.sharelib.init;

import android.content.Context;

public interface IToast {
    void showTip(String msg);
    void showDialogTip(Context context, String msg);
}
