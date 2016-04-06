package com.diagramsf.netrequest.nocacherequest;

import com.diagramsf.net.NetFailedResult;
import com.diagramsf.net.NetResult;

/**
 * 请求网络数据的View，MVP中的V 制定显示状态
 * <p>
 * Created by Diagrams on 2015/8/13 15:18
 */
public interface NoCacheRequestView {

    /** 显示进度条 */
    void showProgress();

    /** 隐藏进度条 */
    void hideProgress();

    /** 提示加载失败 */
    void showRequestFail(NetFailedResult failResult);

    /** 提示加载成功 */
    void showRequestSuccess(NetResult result);
}
