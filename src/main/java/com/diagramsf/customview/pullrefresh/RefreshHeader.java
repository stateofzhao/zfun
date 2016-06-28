package com.diagramsf.customview.pullrefresh;

/**
 * {@link PullRefreshLayout}的headerView
 *
 * Created by Diagrams on 2016/6/28 10:16
 */
public interface RefreshHeader extends OnContentViewScrollListener {
    /** 能够触发刷新的 ContentView滚动的距离 */
    int onCreateTrigRefreshHeight();
    /** 开始执行刷新操作 */
    void onBeginRefresh();
    /** 调用了 {@link PullRefreshLayout#stopRefresh()}后  回调这个方法 */
    void onStopRefresh();
    /** 调用{@link PullRefreshLayout#stopRefresh()}后，动画执行完毕 */
    void onStopRefreshComplete();
}
