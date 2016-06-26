package com.diagramsf.volleynet.refresh;

import android.support.annotation.NonNull;
import com.diagramsf.net.NetContract;
import com.diagramsf.volleynet.NetRequestManager;
import com.diagramsf.volleynet.NetResultFactory;

import java.util.Map;

/**
 * Created by Diagrams on 2016/4/21 11:41
 */
public class RefreshPresenter implements RefreshContract.Presenter {

    private RefreshContract.View mView;
    private RefreshContract.SimpleView mSimpleView;

    private NetRequestManager mRequestManager;

    public RefreshPresenter(@NonNull RefreshContract.View view, @NonNull NetRequestManager manager) {
        mView = view;
        mSimpleView = view;
        mRequestManager = manager;
    }

    public RefreshPresenter(@NonNull RefreshContract.SimpleView view, @NonNull NetRequestManager manager) {
        mSimpleView = view;
        mRequestManager = manager;
    }


    @Override
    public void firstLoadData(final boolean readCache, String url, Map<String, String> postData,
                              String cancelTag,
                              NetResultFactory factory) {
        if (readCache) {
            if (isNoSimpleView()) {
                mView.showFirstCacheRequestProgress();
            }
        } else {
            if (isNoSimpleView()) {
                mView.showFirstNetRequestProgress();
            }
        }

        int type;
        if (readCache) {
            type = NetContract.ONLY_CACHE;
        } else {
            type = NetContract.ONLY_NET_THEN_CACHE;
        }

        mRequestManager.load(url).postData(postData).type(type)
                .errorListener(new NetContract.NetResultErrorListener() {
            @Override
            public void onFailed(NetContract.NetFailResult fail) {
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
        }).listener(new NetContract.NetResultListener() {
            @Override
            public void onSucceed(NetContract.NetSuccessResult result) {
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
        }).into(factory);
    }

    @Override
    public void doRefresh(String url,  Map<String, String> postData, String cancelTag,
                          NetResultFactory factory) {
        mRequestManager.load(url).postData(postData).type(NetContract.ONLY_NET_THEN_CACHE)
                .tag(cancelTag)
                .errorListener(new NetContract.NetResultErrorListener() {
                    @Override
                    public void onFailed(NetContract.NetFailResult fail) {
                        mSimpleView.showRefreshFail(fail);
                    }
                }).listener(new NetContract.NetResultListener() {
            @Override
            public void onSucceed(NetContract.NetSuccessResult result) {
                mSimpleView.showRefreshResult(result);
            }
        }).into(factory);
    }

    @Override
    public void cancelRequest(String cancelTag) {
        mRequestManager.cancel(cancelTag);
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
