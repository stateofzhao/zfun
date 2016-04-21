package com.diagramsf.fragments.refreshl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 下拉刷新和上拉加载更多的抽象Fragment，包含了下拉刷新View和上拉加载更多View
 * <p>
 * Created by Diagrams on 2016/3/14 15:19
 */
public abstract class ViewAbsRLFragmentImpl extends AbsRLFragmentImpl {

    private IPullRefreshView mRefreshView;
    private IPullLoadMoreView mLoadMoreView;

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRefreshView = onCreateRefreshView();
        mLoadMoreView = onCreateLoadMoreView();
        return null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initView(mRefreshView, mLoadMoreView);
        super.onViewCreated(view, savedInstanceState);
    }

    protected abstract IPullLoadMoreView onCreateLoadMoreView();

    protected abstract IPullRefreshView onCreateRefreshView();

}
