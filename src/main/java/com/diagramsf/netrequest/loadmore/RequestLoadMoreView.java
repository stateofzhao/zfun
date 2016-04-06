package com.diagramsf.netrequest.loadmore;

import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetResult;

/**
 * Created by Diagrams on 2015/10/9 17:26
 */
public interface RequestLoadMoreView {
    /** 显示正在进行加载更多的进度条 */
    void showLoadMoreProgress();

    /** 隐藏正在进行加载更多的进度条 */
    void hideLoadMoreProgress();

    /** 加载更多请求失败回调 */
    void loadMoreFail(NetFailedResult result);

    /** 加载更多请求成功回调 */
    void loadMoreFinish(NetResult result);
}
