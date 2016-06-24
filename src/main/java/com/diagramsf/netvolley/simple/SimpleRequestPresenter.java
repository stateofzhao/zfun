package com.diagramsf.netvolley.simple;

import com.diagramsf.net.NetRequest;
import com.diagramsf.netvolley.NetResultFactory;
import com.diagramsf.netvolley.RequestManager;
import com.diagramsf.netvolley.netrepository.NetRequestImpl;

import java.util.Map;

/**
 * Created by Diagrams on 2015/10/15 18:24
 */
public class SimpleRequestPresenter implements SimpleContract.Presenter {

    private SimpleContract.View mView;

    public SimpleRequestPresenter(SimpleContract.View view) {
        mView = view;
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
        NetRequest request = createRequest(url, postData, cancelTag,factory, false);
        request.doRequest(NetRequest.ONLY_CACHE, cancelTag);
    }

    @Override
    public void requestNet(String url, Map<String, String> postData, String cancelTag,
                           NetResultFactory factory, boolean saveCache) {
        if (null != mView) {
            mView.onShowNetProgress();
        }

        NetRequest request = createRequest(url, postData,cancelTag, factory, true);
        if (saveCache) {
            request.doRequest(NetRequest.ONLY_NET_THEN_CACHE, cancelTag);
        } else {
            request.doRequest(NetRequest.ONLY_NET_NO_CACHE, cancelTag);
        }
    }

    @Override
    public void cancelCacheRequest(String cancelTag) {
        RequestManager.getInstance().cancelRequest(cancelTag);
        if (null != mView) {
            mView.onHideCacheLoadProgress();
        }
    }

    @Override
    public void cancelNetRequest(String cancelTag) {
        RequestManager.getInstance().cancelRequest(cancelTag);
        if (null != mView) {
            mView.onHideNetProgress();
        }
    }

    public void onResultFromCache(NetRequest.NetSuccessResult result) {
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

    public void onFailFromCache(NetRequest.NetFailResult result) {
        if (null != mView) {
            mView.onHideCacheLoadProgress();
            mView.onShowCacheFail(result);
        }
    }

    public void onResultFromNet(NetRequest.NetSuccessResult result) {
        if (null != mView) {
            mView.onHideNetProgress();
            mView.onShowNetResult(result);
        }
    }

    public void onFailFromNet(NetRequest.NetFailResult result) {
        if (null != mView) {
            mView.onHideNetProgress();
            mView.onShowNetFail(result);
        }
    }

    private NetRequest createRequest(String url, Map<String, String> postData,String cancelTag,
                                     NetResultFactory factory, final boolean fromNet) {
        NetRequest request = new NetRequestImpl(url, postData, factory);
        request.setResultCallBack(new NetRequest.NetResultCallback() {
            @Override
            public void onSucceed(NetRequest.NetSuccessResult result) {
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

            @Override
            public void onFailed(NetRequest.NetFailResult failResult) {
                if (fromNet) {
                    onFailFromNet(failResult);
                } else {
                    onFailFromCache(failResult);
                }
            }
        });//class end
        request.setDeliverToResultTag(cancelTag);
        return request;
    }

}
