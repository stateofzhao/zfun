package com.zfun.sharelib.core;

import androidx.annotation.NonNull;

/**
 * 执行具体的分享功能。
 * <p/>
 * Created by lizhaofei on 2017/8/4.
 */
public interface IShareHandler {

    void share(@NonNull ShareData shareData);

    /** 是否支持分享 */
    boolean isSupport();

    void init();

    void release();
}
