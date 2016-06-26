package com.diagramsf.volleynet.simple;

import com.diagramsf.net.NetContract;
import com.diagramsf.volleynet.NetResultFactory;
import com.diagramsf.volleynet.NetRequestManager;

import java.util.Map;

/**
 * Created by Diagrams on 2015/10/15 18:24
 */
public class SimpleRequestPresenter implements SimpleContract.Presenter {

    private SimpleContract.View mView;
    private NetRequestManager mNetRequestManager;

    public SimpleRequestPresenter(SimpleContract.View view, NetRequestManager requestManager) {
        mView = view;
        mNetRequestManager = requestManager;
    }

    @Override
    public void start() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void requestCache(String url, Map<String, String> postData, String cancelTag,
                             NetResultFactory factory) {
        if (null != mView) {
            mView.onShowCacheLoadProgress();
        }
        requestData(url, postData, cancelTag, factory, false,NetContract.ONLY_CACHE);
    }

    @Override
    public void requestNet(String url, Map<String, String> postData, String cancelTag,
                           NetResultFactory factory, boolean saveCache) {
        if (null != mView) {
            mView.onShowNetProgress();
        }

        if (saveCache) {
            requestData(url, postData, cancelTag, factory, true,NetContract.ONLY_NET_THEN_CACHE);
        } else {
            requestData(url, postData, cancelTag, factory, true,NetContract.ONLY_NET_NO_CACHE);
        }
    }

    @Override
    public void cancelCacheRequest(String cancelTag) {
        mNetRequestManager.cancel(cancelTag);
        if (null != mView) {
            mView.onHideCacheLoadProgress();
        }
    }

    @Override
    public void cancelNetRequest(String cancelTag) {
        mNetRequestManager.cancel(cancelTag);
        if (null != mView) {
            mView.onHideNetProgress();
        }
    }

    public void onResultFromCache(NetContract.NetSuccessResult result) {
        if (null != mView) {
            mView.onHideCacheLoadProgress();
            mView.onShowCacheResult(result);
        }
    }

    public void onNoResultFromCache() {
        if (null != mView) {
            mView.onHideCacheLoadProgress();
            mView.onShowNoCache();
        }
    }

    public void onFailFromCache(NetContract.NetFailResult result) {
        if (null != mView) {
            mView.onHideCacheLoadProgress();
            mView.onShowCacheFail(result);
        }
    }

    public void onResultFromNet(NetContract.NetSuccessResult result) {
        if (null != mView) {
            mView.onHideNetProgress();
            mView.onShowNetResult(result);
        }
    }

    public void onFailFromNet(NetContract.NetFailResult result) {
        if (null != mView) {
            mView.onHideNetProgress();
            mView.onShowNetFail(result);
        }
    }

    private void requestData(String url, Map<String, String> postData, String cancelTag,
                             NetResultFactory factory, final boolean fromNet, @NetContract.Type int type) {
        mNetRequestManager.load(url).postData(postData).tag(cancelTag).type(type)
                .errorListener(new NetContract.NetResultErrorListener() {
                    @Override
                    public void onFailed(NetContract.NetFailResult fail) {
                        if (fromNet) {
                            onFailFromNet(fail);
                        } else {
                            onFailFromCache(fail);
                        }
                    }
                }).listener(new NetContract.NetResultListener() {
            @Override
            public void onSucceed(NetContract.NetSuccessResult result) {
                if (fromNet) {
                    onResultFromNet(result);
                } else {
                    if (null == result) {
                        onNoResultFromCache();
                    } else {
                        onResultFromCache(result);
                    }
                }
            }
        }).into(factory);
    }

}
