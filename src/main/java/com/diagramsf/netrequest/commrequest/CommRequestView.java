package com.diagramsf.netrequest.commrequest;

import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetResult;

/**
 * Created by Diagrams on 2015/10/15 17:54
 */
public interface CommRequestView {

    /** 显示 加载缓存时的进度条 */
    void onShowCacheLoadProgress();

    /** 隐藏 加载缓存时的进度条 */
    void onHideCacheLoadProgress();

    /** 显示 缓存加载结果 */
    void onShowCacheResult(NetResult result);

    /** 没有读取到缓存（没有缓存） */
    void onShowNoCache();

    /** 读取缓存失败 */
    void onShowCacheFail(NetFailedResult result);

    //----------------------------------------------

    /** 显示 加载网络数据的进度条 */
    void onShowNetProgress();

    /** 隐藏 加载网络数据的进度条 */
    void onHideNetProgress();

    /** 显示 网络加载结果 */
    void onShowNetResult(NetResult result);

    /** 显示 网络加载失败结果 */
    void onShowNetFail(NetFailedResult result);
}
