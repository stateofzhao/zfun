package com.diagramsf.netrequest.commrequest;

import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetRequest;
import com.diagramsf.net.NetResult;
import com.diagramsf.netrequest.OnCacheRequestFinishListener;
import com.diagramsf.netrequest.OnNetRequestFinishListener;
import com.diagramsf.volleybox.NetRequestImpl;
import com.diagramsf.volleybox.NetResultFactory;
import com.diagramsf.volleybox.VolleyUtils;

import java.util.Map;

/**
 * Created by Diagrams on 2015/10/15 18:12
 */
public class RequestInteractorImpl implements RequestInteractor {

    @Override
    public void netRequest(String url, Map<String, String> postData, String cancelTag, NetResultFactory factory,
                           boolean saveCache, final OnNetRequestFinishListener listener) {

        NetRequest request = new NetRequestImpl(url, postData, factory, cancelTag);

        request.setCallBack(new NetRequest.NetRequestCallback() {
            @Override
            public void onSucceed(NetResult result) {
                if (null != listener) {
                    listener.onResultFromNet(result);
                }
            }

            @Override
            public void onFailed(NetFailedResult failResult) {
                if (null != listener) {
                    listener.onFailFromNet(failResult);
                }
            }
        });
        request.setDeliverToResultTag(cancelTag);
        if (saveCache) {
            request.doRequest(NetRequest.ONLY_NET_THEN_CACHE);
        } else {
            request.doRequest(NetRequest.ONLY_NET_NO_CACHE);
        }

    }

    @Override
    public void cacheRequest(String url, Map<String, String> postData, String cancelTag, NetResultFactory factory,
                             final OnCacheRequestFinishListener listener) {
        final NetRequest request = new NetRequestImpl(url, postData, factory, cancelTag);
        request.setCallBack(new NetRequest.NetRequestCallback() {
            @Override
            public void onSucceed(NetResult result) {
                if (null != listener) {
                    if (null == result)
                        listener.onNoResultFromCache();
                    else
                        listener.onResultFromCache(result);
                }
            }

            @Override
            public void onFailed(NetFailedResult failResult) {
                if (null != listener) {
                    listener.onFailFromCache(failResult);
                }
            }
        });
        request.setDeliverToResultTag(cancelTag);
        request.doRequest(NetRequest.ONLY_CACHE);
    }

    @Override
    public void cancelCacheRequest(String cancelTag) {
        VolleyUtils.getInstance().cancelRequest(cancelTag);
    }

    @Override
    public void cancelNetRequest(String cancelTag) {
        VolleyUtils.getInstance().cancelRequest(cancelTag);
    }
}
