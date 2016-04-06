package com.diagramsf.fragments.refreshl.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import com.diagramsf.customview.loadmore.LoadMoreListView;
import com.diagramsf.fragments.refreshl.AbsRLFragmentImpl;

/**
 * 上拉加载更多View
 * <p/>
 * Created by Diagrams on 2016/3/15 16:49
 */
public class LoadMoreListViewRL extends LoadMoreListView implements AbsRLFragmentImpl.PullLoadMoreView {

    public LoadMoreListViewRL(Context context) {
        super(context);
    }

    public LoadMoreListViewRL(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadMoreListViewRL(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LoadMoreListViewRL(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void setOnLoadMoreListener(final AbsRLFragmentImpl.OnLoadMoreListener listener) {
        setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                listener.onLoadMore();
            }
        });
    }

    @Override
    public void destroy() {
        //留白
    }
}