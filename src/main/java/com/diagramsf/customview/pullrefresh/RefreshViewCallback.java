package com.diagramsf.customview.pullrefresh;

public interface RefreshViewCallback {

    /** 能够触发刷新的 ContentView滚动的距离 */
    int getRefreshHeight();

    /**
     * ContentView滚动的距离
     */
    void contentViewScrollDistance(int distance, PullRefreshLayout.State state);

    /** 开始执行刷新操作 */
    void doRefresh();

    /** 调用了 {@link PullRefreshLayout#stopRefresh()}后  回调这个方法 */
    void refreshStop();

    // ----------------目前以下方法并不是成对调用的，而且先后顺序也不能够保证

    /**
     * {@link PullRefreshLayout}一次完整操作(意思是从手指ACTION_DOWN开始，直到刷新完成并且ContentView复位到原始位置 结束或者 没有刷新时
     * ContentView复位到原始位置结束) 开始
     */
    void contentViewBeginScroll();

    /**
     * 一次完整操作 {@link PullRefreshLayout}结束
     */
    void contentViewEndScroll();

}
