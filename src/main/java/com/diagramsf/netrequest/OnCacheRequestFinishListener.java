package com.diagramsf.netrequest;

import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetResult;

/**
 * Created by Diagrams on 2015/10/16 10:09
 */
public interface OnCacheRequestFinishListener {

    /** 从缓存获取到结果 */
    void onResultFromCache(NetResult result);

    /** 从缓存没有读取到结果 */
    void onNoResultFromCache();

    /** 从缓存读取结果失败 */
    void onFailFromCache(NetFailedResult result);
}
