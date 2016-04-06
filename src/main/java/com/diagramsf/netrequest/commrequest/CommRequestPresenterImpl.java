package com.diagramsf.netrequest.commrequest;

import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetResult;
import com.diagramsf.netrequest.OnCacheRequestFinishListener;
import com.diagramsf.netrequest.OnNetRequestFinishListener;
import com.diagramsf.volleybox.NetResultFactory;

import java.util.Map;

/**
 * Created by Diagrams on 2015/10/15 18:24
 */
public class CommRequestPresenterImpl implements CommRequestPresenter,
        OnNetRequestFinishListener, OnCacheRequestFinishListener {

    private CommRequestView mView;
    private RequestInteractor mInteractor;

    public CommRequestPresenterImpl(CommRequestView view) {
        mView = view;
        mInteractor = new RequestInteractorImpl();
    }

    @Override
    public void requestCache(String url, Map<String, String> postData, String cancelTag, NetResultFactory factory) {
        if (null != mView)
            mView.onShowCacheLoadProgress();
        mInteractor.cacheRequest(url, postData, cancelTag, factory, this);
    }

    @Override
    public void requestNet(String url, Map<String, String> postData, String cancelTag, NetResultFactory factory, boolean saveCache) {
        if (null != mView)
            mView.onShowNetProgress();
        mInteractor.netRequest(url, postData, cancelTag, factory, saveCache, this);
    }

    @Override
    public void cancelCacheRequest(String cancelTag) {
        mInteractor.cancelCacheRequest(cancelTag);
        if (null != mView)
            mView.onHideCacheLoadProgress();
    }

    @Override
    public void cancelNetRequest(String cancelTag) {
        mInteractor.cancelNetRequest(cancelTag);
        if (null != mView)
            mView.onHideNetProgress();
    }

    @Override
    public void onResultFromCache(NetResult result) {
        if (null != mView) {
            mView.onHideCacheLoadProgress();
            mView.onShowCacheResult(result);
        }
    }

    @Override
    public void onNoResultFromCache() {
        if (null != mView) {
            mView.onHideCacheLoadProgress();
            mView.onShowNoCache();
        }
    }

    @Override
    public void onFailFromCache(NetFailedResult result) {
        if (null != mView) {
            mView.onHideCacheLoadProgress();
            mView.onShowCacheFail(result);
        }
    }

    @Override
    public void onResultFromNet(NetResult result) {
        if (null != mView) {
            mView.onHideNetProgress();
            mView.onShowNetResult(result);
        }
    }

    @Override
    public void onFailFromNet(NetFailedResult result) {
        if (null != mView) {
            mView.onHideNetProgress();
            mView.onShowNetFail(result);
        }
    }
}
