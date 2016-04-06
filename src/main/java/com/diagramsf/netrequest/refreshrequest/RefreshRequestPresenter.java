package com.diagramsf.netrequest.refreshrequest;

import com.diagramsf.volleybox.NetResultFactory;

/**
 * 能够执行下拉刷新的控制器。
 * <p/>
 * 下拉刷新只有两个步骤：
 * 1.首次加载数据（可以是来自缓存也可以是来自网络，通过{@link #firstLoadData(boolean, String, String, String, NetResultFactory)}
 * 中的第一个参数控制）；
 * 2.执行下拉刷新请求数据{@link #doRefresh(String, String, String, NetResultFactory)}
 * <p/>
 * Created by Diagrams on 2015/10/9 15:03
 */
public interface RefreshRequestPresenter {

    /**
     * 调用这个方法不会执行自动下拉刷新，它会首次去加载数据
     *
     * @param readCache 是否读取缓存
     */
    void firstLoadData(boolean readCache, String url, String postData, String cancelTag,
                       NetResultFactory factory);

    /** 执行刷新请求 */
    void doRefresh(String url, String postData, String cancelTag,
                   NetResultFactory factory);

    void cancelRequest(String cancelTag);
}
