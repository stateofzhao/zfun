package com.diagramsf.netrequest.refreshrequest;

import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetResult;

/**
 * 支持下拉刷新的 视图
 * <p/>
 * Created by Diagrams on 2015/10/10 11:18
 */
public interface RefreshRequestSimpleView {

    /** 显示缓存请求结果 */
    void showFirstCacheResult(NetResult result);

    /** 显示缓存请求失败 */
    void showFirstCacheFail(NetFailedResult failResult);

    /** 显示首次请求缓存时没有读取到缓存 */
    void showFirstNoCache();

    /** 显示网络请求结果 */
    void showFirstNetResult(NetResult result);

    /** 显示网络请求失败 */
    void showFirstNetFail(NetFailedResult failResult);

    /** 显示刷新结果 */
    void showRefreshResult(NetResult result);

    /** 显示刷新失败结果 */
    void showRefreshFail(NetFailedResult failResult);

}
