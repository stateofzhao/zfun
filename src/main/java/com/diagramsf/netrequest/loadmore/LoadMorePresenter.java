package com.diagramsf.netrequest.loadmore;

import com.diagramsf.volleybox.NetResultFactory;

/**
 * Created by Diagrams on 2015/10/9 17:28
 */
public interface LoadMorePresenter {
    void doLoadMore(String url, String postData, String cancelTag, NetResultFactory factory);

    void cancelRequest(String cancelTag);
}
