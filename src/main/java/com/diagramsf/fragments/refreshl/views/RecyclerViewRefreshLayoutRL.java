package com.diagramsf.fragments.refreshl.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import com.diagramsf.customview.pullrefresh.RecyclerViewRefreshLayout;
import com.diagramsf.fragments.refreshl.AbsRLFragmentImpl;

/**
 * 下拉刷新View
 * <p/>
 * Created by Diagrams on 2016/3/15 16:51
 */
public class RecyclerViewRefreshLayoutRL extends RecyclerViewRefreshLayout implements AbsRLFragmentImpl.IPullRefreshView {

    public RecyclerViewRefreshLayoutRL(Context context) {
        super(context);
    }

    public RecyclerViewRefreshLayoutRL(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewRefreshLayoutRL(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecyclerViewRefreshLayoutRL(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
