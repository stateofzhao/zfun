package com.diagramsf.netrequest.refreshrequest;

/**
 * 支持下拉刷新的 视图
 * <p/>
 * Created by Diagrams on 2015/10/9 11:27
 */
public interface RequestRefreshView extends RefreshRequestSimpleView {

    /** 首次进入界面时需要先请求缓存来填充数据，这个是显示请求缓存的进度条 */
    void showFirstCacheRequestProgress();

    /** 隐藏首次缓存请求的进度条 */
    void hideFirstCacheRequestProgress();

    /** 当首次进入界面没有请求到缓存时，需要请求网络数据 这个是显示网络请求的进度条 */
    void showFirstNetRequestProgress();

    /** 隐藏网络请求的进度条 */
    void hideFirstNetRequestProgress();

}
