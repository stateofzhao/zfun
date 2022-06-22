package com.zfun.sharelib.core;

import androidx.annotation.NonNull;

/**
 * 分享管理器
 * <p/>
 * Created by lizhaofei on 2017/8/8 18:01
 */
public interface IShareMgr {
    /**
     * 单个类型分享
     *
     * @param type 分享类型，例如，qq好友，qq空间，微信朋友圈，微信好友，微博等
     * @param shareData 分享的数据
     */
    void share(@ShareConstant.ShareType int type, @NonNull ShareData shareData);

    /** 单个类型分享 */
    void share(@ShareConstant.ShareType int type, @NonNull ISharePlug sharePlug);

    /**
     * 带菜单的分享
     *
     * @param shareMenu 要显示的分享菜单
     * @param shareData 分享数据，注意：需要构建出菜单显示的所有分享类型的数据
     */
    //void shareWithMenu(@NonNull IShareMenu shareMenu, @NonNull ShareData shareData);

    /**
     * 带菜单的分享
     *
     * @param shareMenu 要显示的分享菜单
     * @param sharePlug 处理具体的分享事宜
     */
    //void shareWithMenu(@NonNull IShareMenu shareMenu, @NonNull ISharePlug sharePlug);
}
