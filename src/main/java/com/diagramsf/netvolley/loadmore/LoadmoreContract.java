package com.diagramsf.netvolley.loadmore;

import com.diagramsf.BasePresenter;
import com.diagramsf.BaseView;
import com.diagramsf.net.NetRequest;
import com.diagramsf.netvolley.NetResultFactory;

/**
 * Created by Diagrams on 2016/4/21 11:25
 */
public interface LoadmoreContract {

    interface View extends BaseView<BasePresenter>{
        /** 显示正在进行加载更多的进度条 */
        void showLoadMoreProgress();

        /** 隐藏正在进行加载更多的进度条 */
        void hideLoadMoreProgress();

        /** 加载更多请求失败回调 */
        void loadMoreFail(NetRequest.NetFailResult result);

        /** 加载更多请求成功回调 */
        void loadMoreFinish(NetRequest.NetSuccessResult result);
    }// class end

    interface Presenter extends BasePresenter {
        void doLoadmore(String url, String postData, String cancelTag, NetResultFactory factory);

        void cancelRequest(String cancelTag);
    }
}
