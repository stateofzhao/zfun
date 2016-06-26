package com.diagramsf.fragments.refreshl;

import android.support.annotation.NonNull;
import com.diagramsf.volleynet.refresh.RefreshContract;

import java.util.Map;

/**
 * 处理下拉刷新逻辑
 * <p>
 * Created by Diagrams on 2016/6/23 14:40
 */
public class RefreshHandler {

    private IPullRefreshView mVRefresh;

    private OnLifeCallback mCallback;

    private RefreshContract.Presenter mPresenter;

    /** 下拉刷新View接口 */
    public interface IPullRefreshView {

        /** 设置上拉刷新的监听器 */
        void setOnRefreshListener(OnRefreshListener listener);

        /** 执行自动下拉刷新 */
        void autoRefresh();

        /** 停止刷新状态 */
        void stopRefresh();

        /** 销毁View */
        void destroy();

        /** 是否能够下拉刷新 */
        void setEnableRefresh(boolean enableRefresh);

    }//end class IPullRefreshView

    public interface OnRefreshListener {
        /** 触发刷新操作的回调事件 */
        void onRefresh();

        /** 刷新状态恢复到正常状态 */
        void onRefreshComplete();

    }//end class OnRefreshListener

    public interface OnLifeCallback {
        /** 下拉刷新 请求网络数据的 URL */
        String onCreateRefreshURL();

        /** 下拉刷新 请求网络数据的 PostData */
        Map<String,String> onCreateRefreshPostData();

        /** 开始执行刷新操作 */
        void onBeginRefresh();

        /** 刷新结果 */
        void onRefreshResult();
    }

    public void init(@NonNull IPullRefreshView refreshView, @NonNull RefreshContract.Presenter presenter,
                     @NonNull OnLifeCallback callback) {
        mVRefresh = refreshView;
        mPresenter = presenter;
        mCallback = callback;
        mVRefresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCallback.onBeginRefresh();
            }

            @Override
            public void onRefreshComplete() {

            }
        });
    }

    /** 首次加载数据 */
    public void firstLoad() {
        final String url = mCallback.onCreateRefreshURL();
        final Map<String,String> postData = mCallback.onCreateRefreshPostData();

    }


}
