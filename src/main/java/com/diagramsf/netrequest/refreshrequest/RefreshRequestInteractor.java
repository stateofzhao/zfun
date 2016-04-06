package com.diagramsf.netrequest.refreshrequest;

import com.diagramsf.netrequest.OnCacheRequestFinishListener;
import com.diagramsf.netrequest.OnNetRequestFinishListener;
import com.diagramsf.volleybox.NetResultFactory;

/**
 * 上拉刷新请求交互器
 * <p>
 * Created by Diagrams on 2015/10/9 11:20
 */
public interface RefreshRequestInteractor {

    /** 请求缓存数据 */
    void requestCache(String url, String postData, String cancelTag, NetResultFactory factory,
                      OnCacheRequestFinishListener listener);

    /** 请求网络数据，会跳过缓存，但是会缓存请求结果 */
    void requestNet(String url, String postData, String cancelTag, NetResultFactory factory,
                    OnNetRequestFinishListener listener);

    /** 取消缓存请求 */
    void cancelCacheRequest(String cancelTag);

    /** 取消网络请求 */
    void cancelNetRequest(String cancelTag);
}
