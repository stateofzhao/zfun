package com.diagramsf.netrequest.commrequest;

import com.diagramsf.volleybox.NetResultFactory;

import java.util.Map;

/**
 * Created by Diagrams on 2015/10/15 18:07
 */
public interface CommRequestPresenter {

    /** 执行缓存请求， 不会请求网络 */
    void requestCache(String url, Map<String, String> postData, String cancelTag, NetResultFactory factory);

    /** 执行网络请求， 不会读取缓存 */
    void requestNet(String url, Map<String, String> postData, String cancelTag, NetResultFactory factory, boolean saveCache);

    void cancelCacheRequest(String cancelTag);

    void cancelNetRequest(String cancelTag);

}
