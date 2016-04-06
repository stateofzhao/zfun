package com.diagramsf.netrequest;

import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetResult;

/**
 * Created by Diagrams on 2015/10/16 10:09
 */
public interface OnNetRequestFinishListener {

    /** 从网络上获取到结果 */
    void onResultFromNet(NetResult result);

    /** 从网络读取结果失败 */
    void onFailFromNet(NetFailedResult result);
}
