package com.zfun.sharelib.core;

import androidx.annotation.NonNull;

/**
 * 能够被{@link IShareMgr}管理的分享插件
 * <p/>
 * Created by lizhaofei on 2017/8/7 18:03
 */
public interface ISharePlug {
    void share(@ShareConstant.ShareType int type, @NonNull IShareHandler shareHandler);

    void cancel();
}
