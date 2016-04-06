package com.diagramsf.netrequest.loadmore;

import android.support.annotation.NonNull;
import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetResult;
import com.diagramsf.netrequest.OnNetRequestFinishListener;
import com.diagramsf.netrequest.nocacherequest.NoCacheRequestInteractor;
import com.diagramsf.netrequest.nocacherequest.NoCacheRequestInteractorImpl;
import com.diagramsf.volleybox.NetResultFactory;

/**
 * Created by Diagrams on 2015/10/9 17:30
 */
public class LoadMorePresenterImpl implements LoadMorePresenter {

    private RequestLoadMoreView mView;
    private NoCacheRequestInteractor mInteractor;

    public LoadMorePresenterImpl(@NonNull RequestLoadMoreView view) {
        mView = view;
        mInteractor = new NoCacheRequestInteractorImpl();
    }

    @Override
    public void doLoadMore(String url, String postData, String cancelTag, NetResultFactory factory) {
        mView.showLoadMoreProgress();
        mInteractor.request(url, postData, cancelTag, factory, new OnNetRequestFinishListener() {
            @Override
            public void onResultFromNet(NetResult result) {
                mView.hideLoadMoreProgress();
                mView.loadMoreFinish(result);
            }

            @Override
            public void onFailFromNet(NetFailedResult result) {
                mView.hideLoadMoreProgress();
                mView.loadMoreFail(result);
            }
        });
    }

    @Override
    public void cancelRequest(String cancelTag) {
        mInteractor.cancelRequest(cancelTag);
    }
}
