package com.diagramsf.netvolley.loadmore;

import android.support.annotation.NonNull;
import com.diagramsf.net.NetRequest;
import com.diagramsf.netvolley.NetResultFactory;
import com.diagramsf.netvolley.RequestManager;
import com.diagramsf.netvolley.netrepository.NetRequestImpl;
import com.google.common.base.Preconditions;

/**
 * Created by Diagrams on 2016/4/21 11:27
 */
public class LoadmorePresenter implements LoadmoreContract.Presenter{

    LoadmoreContract.View mView;

    public LoadmorePresenter(@NonNull LoadmoreContract.View view){
        Preconditions.checkNotNull(view);
        mView = view;
    }

    @Override
    public void doLoadmore(String url, String postData, String cancelTag, NetResultFactory factory) {
        NetRequest request = new NetRequestImpl(url, postData, factory);
        request.setResultCallBack(new NetRequest.NetResultCallback() {
            @Override
            public void onSucceed(NetRequest.NetSuccessResult result) {
                mView.hideLoadMoreProgress();
                mView.loadMoreFinish(result);
            }

            @Override
            public void onFailed(NetRequest.NetFailResult fail) {
                mView.hideLoadMoreProgress();
                mView.loadMoreFail(fail);
            }
        });
        request.doRequest(NetRequest.ONLY_NET_NO_CACHE,cancelTag);
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
}
