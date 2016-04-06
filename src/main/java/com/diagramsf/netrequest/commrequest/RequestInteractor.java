package com.diagramsf.netrequest.commrequest;

import com.diagramsf.netrequest.OnCacheRequestFinishListener;
import com.diagramsf.netrequest.OnNetRequestFinishListener;
import com.diagramsf.volleybox.NetResultFactory;

import java.util.Map;

/**
 * 数据来源
 * <p/>
 * Created by Diagrams on 2015/10/15 17:51
 */
public interface RequestInteractor {

    void netRequest(String url, Map<String, String> postParams, String cancelTag, NetResultFactory factory, boolean saveCache,
                    OnNetRequestFinishListener listener);

    void cacheRequest(String url, Map<String, String> postParams, String cancelTag, NetResultFactory factory,
                      OnCacheRequestFinishListener listener);

    void cancelCacheRequest(String cancelTag);

    void cancelNetRequest(String cancelTag);

}
