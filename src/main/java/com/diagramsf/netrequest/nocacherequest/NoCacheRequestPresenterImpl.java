package com.diagramsf.netrequest.nocacherequest;

import android.support.annotation.NonNull;
import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetResult;
import com.diagramsf.netrequest.OnNetRequestFinishListener;
import com.diagramsf.volleybox.NetResultFactory;

/**
 * 执行网络请求
 * <p>
 * Created by Diagrams on 2015/8/13 15:44
 */
public class NoCacheRequestPresenterImpl implements NoCacheRequestPresenter, OnNetRequestFinishListener {

    NoCacheRequestView mRequestView;
    NoCacheRequestInteractor mRequestInteractor;

    /**
     * 会调用默认的{@link NoCacheRequestInteractorImpl}来执行网络请求
     *
     * @param view View接口用来更新实际View的状态
     */
    public NoCacheRequestPresenterImpl(NoCacheRequestView view) {
        mRequestView = view;
        mRequestInteractor = new NoCacheRequestInteractorImpl();
    }

    /**
     * @param view       View接口用来更新实际View的状态
     * @param interactor 指定网络请求实现，来执行网络请求
     */
    public NoCacheRequestPresenterImpl(NoCacheRequestView view, @NonNull NoCacheRequestInteractor interactor) {
        mRequestView = view;
        mRequestInteractor = interactor;
    }

    @Override
    public void doRequest(String url, String postData, String cancelTag, NetResultFactory factory) {
        if (null != mRequestView)
            mRequestView.showProgress();
        mRequestInteractor.request(url, postData, cancelTag, factory, this);
    }

    @Override
    public void cancelRequest(String cancelTag) {
        mRequestInteractor.cancelRequest(cancelTag);
    }

    @Override
    public void onResultFromNet(NetResult result) {
        if (null != mRequestView) {
            mRequestView.hideProgress();
            mRequestView.showRequestSuccess(result);
        }
    }

    @Override
    public void onFailFromNet(NetFailedResult result) {
        if (null != mRequestView) {
            mRequestView.hideProgress();
            mRequestView.showRequestFail(result);
        }
    }
}
