package com.diagramsf.customview.loadmore;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义的上拉加载更多的ListView
 * <p>
 * Created by Diagrams on 2015/7/9 18:11
 */
public class LoadMoreListView extends ListView implements AbsListView.OnScrollListener {

    private List<OnScrollListener> mOnScrollListeners;
    private OnLoadMoreListener mOnLoadMoreListener;

    private boolean mIsLoadMoreFail;
    private boolean mIsLoadMoreNormal;
    private boolean mIsLoadMoreNothing;
    private boolean lastItemVisible;
    private boolean mLoadMoreEnable = true;
    private boolean mIsLoadingMore = false;//是否正在执行加载更多

    //12111 车系-图片，进入任意一类型的图片列表，上拉加载卡住了  2015/11/4  lzf  start
    private boolean mTouchUp = true;//这个默认值要是true，因为有时候  手指非常快速的滑动下ListView，会造成ACTION_DOWN和ACTION_MOVE都不执行，不知道为啥
    private float mLastTouchY = Float.MAX_VALUE;//必须有一个最大值的初始值,因为如果手指非常快速的滑动下ListView，会造成ACTION_DOWN方法不执行，具体原因不知道
    //12111 车系-图片，进入任意一类型的图片列表，上拉加载卡住了  2015/11/4  lzf  end

    private LoadMoreFooterView mIFooterView;
    private View mFooterView;

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    /** footerView接口 */
    public interface LoadMoreFooterView {
        View onCreateFooterView(Context context,ViewGroup parent);

        void onLoadMoreComplete();

        void onLoadMoreNothing();

        void onLoadMoreFailed();

        void onLoadMoreNormal();

        void onHideLoadMoreFooterView();

        void onShowLoadMoreFooterView();
    }

    public LoadMoreListView(Context context) {
        super(context);
        init();
    }

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /** 必须在{@link #setAdapter(ListAdapter)} 之前调用 */
    public void initLoadMoreFooterView(LoadMoreFooterView footerView){
        mIFooterView = footerView;
        mFooterView = footerView.onCreateFooterView(getContext(),this);
        addFooterView(mFooterView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        final int actionMasked = ev.getActionMasked();
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mLastTouchY = ev.getY();
            mTouchUp = false;
            //12111 车系-图片，进入任意一类型的图片列表，上拉加载卡住了  2015/11/4  lzf  start
        } else if (actionMasked == MotionEvent.ACTION_MOVE || actionMasked == MotionEvent.ACTION_SCROLL) {
            mTouchUp = (ev.getY() - mLastTouchY) < 0;
            mLastTouchY = ev.getY();
            //12111 车系-图片，进入任意一类型的图片列表，上拉加载卡住了  2015/11/4  lzf  end
        }
        return result;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        for (OnScrollListener listener : mOnScrollListeners) {
            listener.onScrollStateChanged(view, scrollState);
        }

        if (mIsLoadingMore) {
            return;
        }

        // 是否滚动到底部
        if (scrollState == SCROLL_STATE_IDLE && lastItemVisible && mLoadMoreEnable && !mIsLoadMoreNothing && mTouchUp) {
            if (null != mOnLoadMoreListener) {
                mIsLoadingMore = true;
                mOnLoadMoreListener.onLoadMore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        for (OnScrollListener listener : mOnScrollListeners) {
            listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }

        lastItemVisible = (totalItemCount > 0)
                && (firstVisibleItem + visibleItemCount >= (totalItemCount - 1))
                /*&& visibleItemCount != totalItemCount*/;
        //#14193 by chenchong 16/1/11
        //此处原先在所有item在同一屏显示时,认为不是最后一条,导致下拉上拉加载判定无法通过
    }

    private void init() {
        mOnScrollListeners = new ArrayList<>();
        super.setOnScrollListener(this);
    }

    public View getFooterView() {
        return mFooterView;
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        if (null == l) {
            return;
        }
        mOnScrollListeners.add(l);
    }

    public void setOnLoadMoreListener(OnLoadMoreListener l) {
        mOnLoadMoreListener = l;
    }

    public void loadMoreComplete() {
        mIsLoadingMore = false;
        mIFooterView.onLoadMoreComplete();
    }

    /** 切换成 沒有更多了 的狀態 */
    public void loadMoreNothing() {
        loadMoreComplete();
        if (mIsLoadMoreNothing) {
            mIFooterView.onLoadMoreNothing();
            return;
        }
        mIsLoadMoreNothing = true;
        mIsLoadMoreFail = false;
        mIsLoadMoreNormal = false;
        mIFooterView.onLoadMoreNothing();
    }

    /** 切换成 加载更多失败状态 */
    public void loadMoreFailed() {
        loadMoreComplete();
        if (mIsLoadMoreFail) {
            mIFooterView.onLoadMoreFailed();
            return;
        }
        mIsLoadMoreFail = true;
        mIsLoadMoreNormal = false;
        mIsLoadMoreNothing = false;
        mIFooterView.onLoadMoreFailed();
        mIsLoadingMore = false;
    }

    /** 切换成 能够上拉加载更多的状态 */
    public void loadMoreNormal() {
        if (mIsLoadMoreNormal) {
            return;
        }
        mIsLoadMoreNormal = true;
        mIsLoadMoreNothing = false;
        mIsLoadMoreFail = false;
        mIFooterView.onLoadMoreNormal();
    }

    public boolean isLoadMoreFailed() {
        return mIsLoadMoreFail;
    }

    public boolean isLoadMoreNothing() {
        return mIsLoadMoreNothing;
    }

    public boolean isIsLoadMoreNormal() {
        return mIsLoadMoreNormal;
    }

    public boolean isEnableLoadMore() {
        return mLoadMoreEnable;
    }

    /** 设置上拉加载更多是否可用,如果不可用,则底部布局隐藏 */
    public void setLoadMoreEnable(boolean enable) {
        if (mLoadMoreEnable == enable) {
            return;
        }
        mLoadMoreEnable = enable;
        if (mLoadMoreEnable) {
            mIFooterView.onShowLoadMoreFooterView();
        } else {
            mIFooterView.onHideLoadMoreFooterView();
        }
    }

}
