package com.diagramsf.netvolley.refresh;

import android.support.annotation.NonNull;
import com.diagramsf.net.NetRequest;
import com.diagramsf.netvolley.NetResultFactory;
import com.diagramsf.netvolley.RequestManager;
import com.diagramsf.netvolley.netrepository.NetRequestImpl;

/**
 * Created by Diagrams on 2016/4/21 11:41
 */
public class RefreshPresenter implements RefreshContract.Presenter {

    private RefreshContract.View mView;
    private RefreshContract.SimpleView mSimpleView;

    public RefreshPresenter(@NonNull RefreshContract.View view) {
        mView = view;
        mSimpleView = view;
    }

    public RefreshPresenter(@NonNull RefreshContract.SimpleView view) {
        mSimpleView = view;
    }


    @Override
    public void firstLoadData(final boolean readCache, String url, String postData, String cancelTag, NetResultFactory factory) {
        NetRequest request = new NetRequestImpl(url, postData, factory);
        if (readCache) {
            if (isNoSimpleView()) {
                mView.showFirstCacheRequestProgress();
            }
        } else {
            if (isNoSimpleView()) {
                mView.showFirstNetRequestProgress();
            }
        }
        request.setResultCallBack(new NetRequest.NetResultCallback() {
            @Override
            public void onSucceed(NetRequest.NetSuccessResult result) {
                if (readCache) {
                    if (null == result) {//无缓存
                        if (isNoSimpleView()) {
                            mView.hideFirstCacheRequestProgress();
                        }
                        mSimpleView.showFirstNoCache();
                    } else {//有缓存
                        if (isNoSimpleView()) {
                            mView.hideFirstCacheRequestProgress();
                        }
                        mSimpleView.showFirstCacheResult(result);
                    }
                } else {
                    if (isNoSimpleView()) {
                        mView.hideFirstNetRequestProgress();
                    }
                    mSimpleView.showFirstNetResult(result);
                }
            }

            @Override
            public void onFailed(NetRequest.NetFailResult fail) {
                if (readCache) {
                    if (isNoSimpleView()) {
                        mView.hideFirstCacheRequestProgress();
                    }
                    mSimpleView.showFirstCacheFail(fail);
                } else {
                    if (isNoSimpleView()) {
                        mView.hideFirstNetRequestProgress();
                    }
                    mSimpleView.showFirstNetFail(fail);
                }
            }
        });

        if (readCache) {
            request.doRequest(NetRequest.ONLY_CACHE, cancelTag);
        } else {
            request.doRequest(NetRequest.ONLY_NET_THEN_CACHE, cancelTag);
        }
    }

    @Override
    public void doRefresh(String url, String postData, String cancelTag, NetResultFactory factory) {
        NetRequest request = new NetRequestImpl(url, postData, factory);
        request.setResultCallBack(new NetRequest.NetResultCallback() {
            @Override
            public void onSucceed(NetRequest.NetSuccessResult result) {
                mSimpleView.showRefreshResult(result);
            }

            @Override
            public void onFailed(NetRequest.NetFailResult fail) {
                mSimpleView.showRefreshFail(fail);
            }
        });
        request.doRequest(NetRequest.ONLY_NET_THEN_CACHE, cancelTag);
    }

    @Override
    public void cancelRequest(String cancelTag) {
        RequestManager.getInstance().cancelRequest(cancelTag);
    }

    @Override
    public void start() {

    }

    @Override
    public void destroy() {

    }

    //是否是SimpleView
    private boolean isNoSimpleView() {
        return null != mView;
    }
}
