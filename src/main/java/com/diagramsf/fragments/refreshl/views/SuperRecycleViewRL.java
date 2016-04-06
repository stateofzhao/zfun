package com.diagramsf.fragments.refreshl.views;

import android.content.Context;
import android.util.AttributeSet;
import com.diagramsf.customview.loadmore.SuperRecyclerView;
import com.diagramsf.fragments.refreshl.AbsRLFragmentImpl;

/**
 * 上拉加载更多View
 * <p/>
 * Created by Diagrams on 2016/3/15 16:48
 */
public class SuperRecycleViewRL extends SuperRecyclerView implements AbsRLFragmentImpl.PullLoadMoreView {

    public SuperRecycleViewRL(Context context) {
        super(context);
    }

    public SuperRecycleViewRL(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SuperRecycleViewRL(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setOnLoadMoreListener(final AbsRLFragmentImpl.OnLoadMoreListener listener) {
        setListener(new Listener() {
            @Override
            public void onLoad() {
                listener.onLoadMore();
            }
        });
    }

    @Override
    public void loadMoreNormal() {
        setComplete();
    }

    @Override
    public void loadMoreFailed() {
        setFailed();
    }

    @Override
    public void loadMoreNothing() {
        setFinal();
    }

    @Override
    public void loadMoreComplete() {
        setComplete();
    }

    @Override
    public void destroy() {

    }
}