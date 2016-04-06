package com.diagramsf.netrequest.nocacherequest;

import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetRequest;
import com.diagramsf.net.NetResult;
import com.diagramsf.netrequest.OnNetRequestFinishListener;
import com.diagramsf.volleybox.NetRequestImpl;
import com.diagramsf.volleybox.NetResultFactory;
import com.diagramsf.volleybox.VolleyUtils;

/**
 * 执行无缓存的网络请求,既不会读取缓存也不会保存缓存
 * <p/>
 * Created by Diagrams on 2015/8/13 15:30
 */
public class NoCacheRequestInteractorImpl implements NoCacheRequestInteractor {

    private Object mTag;

    @Override
    public void setDeliverToResultTag(Object tag) {
        mTag = tag;
    }

    /** 执行无缓存的网络请求,既不会读取缓存也不会保存缓存; */
    @Override
    public void request(String url, String postData, String cancelTag, NetResultFactory factory,
                        final OnNetRequestFinishListener listener) {

        NetRequest request = new NetRequestImpl(url, postData, factory, cancelTag);

        if (null != mTag) {
            request.setDeliverToResultTag(mTag);
        }
        request.setCallBack(new NetRequest.NetRequestCallback() {
            @Override
            public void onSucceed(NetResult result) {
                listener.onResultFromNet(result);
            }

            @Override
            public void onFailed(NetFailedResult failResult) {
                listener.onFailFromNet(failResult);
            }
        });
        request.doRequest(NetRequest.ONLY_NET_NO_CACHE);
    }

    @Override
    public void cancelRequest(String cancelTag) {
        VolleyUtils.getInstance().cancelRequest(cancelTag);
    }
}
