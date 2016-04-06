package com.diagramsf.netrequest.refreshrequest;

import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetRequest;
import com.diagramsf.net.NetResult;
import com.diagramsf.netrequest.OnCacheRequestFinishListener;
import com.diagramsf.netrequest.OnNetRequestFinishListener;
import com.diagramsf.volleybox.NetRequestImpl;
import com.diagramsf.volleybox.NetResultFactory;
import com.diagramsf.volleybox.VolleyUtils;

/**
 * Created by Diagrams on 2015/10/9 14:34
 */
public class RefreshRequestInteractorImpl implements RefreshRequestInteractor {

    @Override
    public void requestCache(String url, String postData, String cancelTag, NetResultFactory factory,
                             final OnCacheRequestFinishListener listener) {

        NetRequest request = new NetRequestImpl(url,postData,factory,cancelTag);

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
        request.doRequest(NetRequest.ONLY_CACHE);

    }

    /** 会执行跳过缓存的网络请求，但是会保存请求的结果*/
    @Override
    public void requestNet(String url, String postData, String cancelTag, NetResultFactory factory,
                           final OnNetRequestFinishListener listener) {
        NetRequest request = new NetRequestImpl(url,postData,factory,cancelTag);
        request.setCallBack(new NetRequest.NetRequestCallback() {
            @Override
            public void onSucceed(NetResult result) {
                if (null != listener)
                    listener.onResultFromNet(result);
            }

            @Override
            public void onFailed(NetFailedResult failResult) {
                if (null != listener)
                    listener.onFailFromNet(failResult);
            }
        });
        request.doRequest(NetRequest.ONLY_NET_THEN_CACHE);
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
