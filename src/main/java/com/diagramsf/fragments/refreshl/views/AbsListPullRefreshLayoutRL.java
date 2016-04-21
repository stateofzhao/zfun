package com.diagramsf.fragments.refreshl.views;

import android.content.Context;
import android.util.AttributeSet;
import com.diagramsf.customview.pullrefresh.AbsListPullRefreshLayout;
import com.diagramsf.fragments.refreshl.AbsRLFragmentImpl;

/**
 * 下拉刷新View
 * <p/>
 * Created by Diagrams on 2016/3/15 16:50
 */
public class AbsListPullRefreshLayoutRL extends AbsListPullRefreshLayout implements AbsRLFragmentImpl.IPullRefreshView {

    public AbsListPullRefreshLayoutRL(Context context) {
        super(context);
    }

    public AbsListPullRefreshLayoutRL(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsListPullRefreshLayoutRL(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AbsListPullRefreshLayoutRL(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setOnRefreshListener(final AbsRLFragmentImpl.OnRefreshListener listener) {
        setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                listener.onRefresh();
            }

            @Override
            public void onRefreshComplete() {
                listener.onRefreshComplete();
            }
        });
    }

    @Override
    public void destroy() {
        cancel();
    }

    @Override
    public void setEnableRefresh(boolean enableRefresh) {
        if (enableRefresh) {
            enableRefresh();
        } else {
            disEnableRefresh(false);
        }
    }
}