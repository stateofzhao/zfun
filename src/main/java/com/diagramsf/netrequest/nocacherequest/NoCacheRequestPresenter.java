package com.diagramsf.netrequest.nocacherequest;

import com.diagramsf.volleybox.NetResultFactory;

/**
 * 网络请求的逻辑控制器，MVP中的P 负责处理 {@link NoCacheRequestInteractor}与 {@link NoCacheRequestView}的交互;
 * <p>
 * Created by Diagrams on 2015/8/13 15:22
 */
public interface NoCacheRequestPresenter {

     void doRequest(String url, String postData, String cancelTag, NetResultFactory factory);

    void cancelRequest(String cancelTag);

}
