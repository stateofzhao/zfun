package com.diagramsf.netrequest.refreshrequest;

import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetResult;
import com.diagramsf.netrequest.OnCacheRequestFinishListener;
import com.diagramsf.netrequest.OnNetRequestFinishListener;
import com.diagramsf.netrequest.nocacherequest.NoCacheRequestInteractor;
import com.diagramsf.netrequest.nocacherequest.NoCacheRequestInteractorImpl;
import com.diagramsf.volleybox.NetResultFactory;

/**
 * 执行下拉刷新控制类
 * <p/>
 * Created by Diagrams on 2015/10/9 15:13
 */
public class RefreshRequestPresenterImpl implements RefreshRequestPresenter {


    private RefreshRequestInteractor mInteractor;
    private NoCacheRequestInteractor mNoCacheInteractor;
    private RequestRefreshView mView;
    private RefreshRequestSimpleView mSimpleView;

    public RefreshRequestPresenterImpl(RequestRefreshView view) {
        mView = view;
        mSimpleView = view;
        init();
    }

    public RefreshRequestPresenterImpl(RefreshRequestSimpleView view) {
        mSimpleView = view;
        init();
    }

    private void init() {
        mInteractor = new RefreshRequestInteractorImpl();
        mNoCacheInteractor = new NoCacheRequestInteractorImpl();
    }

    //是否有View
    private boolean isViewWork() {
        return null != mView || null != mSimpleView;
    }

    //是否是SimpleView
    private boolean isNoSimpleView() {
        return null != mView;
    }


    @Override

    public void firstLoadData(boolean readCache, String url, String postData, String cancelTag,
                              final NetResultFactory factory) {
        if (readCache) {
            if (isViewWork()) {
                if (isNoSimpleView())
                    mView.showFirstCacheRequestProgress();
            }
            mInteractor.requestCache(url, postData, cancelTag, factory, new OnCacheRequestFinishListener() {
                @Override
                public void onResultFromCache(NetResult result) {

                    if (!isViewWork())
                        return;
                    if (isNoSimpleView())
                        mView.hideFirstCacheRequestProgress();
                    mSimpleView.showFirstCacheResult(result);
                }

                @Override
                public void onNoResultFromCache() {

                    if (!isViewWork())
                        return;
                    if (isNoSimpleView())
                        mView.hideFirstCacheRequestProgress();
                    mSimpleView.showFirstNoCache();
                }

                @Override
                public void onFailFromCache(NetFailedResult result) {
                    if (!isViewWork())
                        return;
                    if (isNoSimpleView())
                        mView.hideFirstCacheRequestProgress();
                    mSimpleView.showFirstCacheFail(result);
                }

            });
        } else {
            if (isViewWork()) {
                if (isNoSimpleView())
                    mView.showFirstNetRequestProgress();
            }
            mNoCacheInteractor.request(url, postData, cancelTag, factory, new OnNetRequestFinishListener() {
                @Override
                public void onResultFromNet(NetResult result) {
                    if (!isViewWork())
                        return;
                    mSimpleView.showFirstNetResult(result);
                    if (isNoSimpleView())
                        mView.hideFirstNetRequestProgress();
                }

                @Override
                public void onFailFromNet(NetFailedResult result) {
                    if (!isViewWork())
                        return;
                    if (isNoSimpleView())
                        mView.hideFirstNetRequestProgress();
                    mSimpleView.showFirstNetFail(result);
                }
            });
        }
    }

    public void doRefresh(String url, String postData, String cancelTag,
                          NetResultFactory factory) {
        mInteractor.requestNet(url, postData, cancelTag, factory, new OnNetRequestFinishListener() {
            @Override
            public void onResultFromNet(NetResult result) {
                if (isViewWork()) {
                    mSimpleView.showRefreshResult(result);
                }
            }

            @Override
            public void onFailFromNet(NetFailedResult result) {
                if (isViewWork()) {
                    mSimpleView.showRefreshFail(result);
                }
            }
        });
    }


    @Override
    public void cancelRequest(String cancelTag) {
        mInteractor.cancelCacheRequest(cancelTag);
        mInteractor.cancelNetRequest(cancelTag);
        mNoCacheInteractor.cancelRequest(cancelTag);
    }
}
