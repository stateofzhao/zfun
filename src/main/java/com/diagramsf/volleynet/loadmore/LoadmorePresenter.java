package com.diagramsf.volleynet.loadmore;

import android.support.annotation.NonNull;
import com.diagramsf.net.NetContract;
import com.diagramsf.volleynet.NetRequestManager;
import com.diagramsf.volleynet.NetResultFactory;
import com.google.common.base.Preconditions;

import java.util.Map;

/**
 * Created by Diagrams on 2016/4/21 11:27
 */
public class LoadmorePresenter implements LoadmoreContract.Presenter {

    LoadmoreContract.View mView;
    NetRequestManager mRequestManager;

    public LoadmorePresenter(@NonNull LoadmoreContract.View view, @NonNull NetRequestManager manager) {
        Preconditions.checkNotNull(view);
        mView = view;
        mRequestManager = manager;
    }

    @Override
    public void doLoadmore(String url, Map<String, String> postData, String cancelTag,
                           NetResultFactory factory) {
        mRequestManager.load(url).postData(postData).tag(cancelTag).type(NetContract.ONLY_NET_NO_CACHE)
                .errorListener(new NetContract.NetResultErrorListener() {
                    @Override
                    public void onFailed(NetContract.NetFailResult fail) {
                        mView.hideLoadMoreProgress();
                        mView.loadMoreFail(fail);
                    }
                }).listener(new NetContract.NetResultListener() {
            @Override
            public void onSucceed(NetContract.NetSuccessResult result) {
                mView.hideLoadMoreProgress();
                mView.loadMoreFinish(result);
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
}
