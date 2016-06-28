package com.diagramsf.customview.pullrefresh;

/**
 * 被刷新View的滚动监听器
 * <p>
 * Created by Diagrams on 2016/6/28 10:14
 */
public interface OnContentViewScrollListener {
    /**
     * ContentView滚动的距离
     */
    void onContentViewScrollDistance(int distance, PullRefreshLayout.State state);

    // ----------------目前以下方法并不是成对调用的，而且先后顺序也不能够保证
    /**
     * {@link PullRefreshLayout}一次完整操作(意思是从手指ACTION_DOWN开始，直到刷新完成并且ContentView复位到原始位置 结束或者 没有刷新时
     * ContentView复位到原始位置结束) 开始
     */
    void onContentViewBeginScroll();

    /**
     * 一次完整操作 {@link PullRefreshLayout}结束
     */
    void onContentViewEndScroll();
}
