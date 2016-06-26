package com.diagramsf.volleynet.refresh;

import com.diagramsf.BasePresenter;
import com.diagramsf.BaseView;
import com.diagramsf.net.NetContract;
import com.diagramsf.volleynet.NetResultFactory;

import java.util.Map;

/**
 * Created by Diagrams on 2016/4/21 11:36
 */
public interface RefreshContract {

    /** 支持下拉刷新的 视图 */
    interface SimpleView extends BaseView<BasePresenter> {

        /** 显示缓存请求结果 */
        void showFirstCacheResult(NetContract.NetSuccessResult result);

        /** 显示缓存请求失败 */
        void showFirstCacheFail(NetContract.NetFailResult failResult);

        /** 显示首次请求缓存时没有读取到缓存 */
        void showFirstNoCache();

        /** 显示网络请求结果 */
        void showFirstNetResult(NetContract.NetSuccessResult result);

        /** 显示网络请求失败 */
        void showFirstNetFail(NetContract.NetFailResult failResult);

        /** 显示刷新结果 */
        void showRefreshResult(NetContract.NetSuccessResult result);

        /** 显示刷新失败结果 */
        void showRefreshFail(NetContract.NetFailResult failResult);

    }// class end

    /** 支持下拉刷新的 视图 */
    interface View extends SimpleView {

        /** 首次进入界面时需要先请求缓存来填充数据，这个是显示请求缓存的进度条 */
        void showFirstCacheRequestProgress();

        /** 隐藏首次缓存请求的进度条 */
        void hideFirstCacheRequestProgress();

        /** 当首次进入界面没有请求到缓存时，需要请求网络数据 这个是显示网络请求的进度条 */
        void showFirstNetRequestProgress();

        /** 隐藏网络请求的进度条 */
        void hideFirstNetRequestProgress();

    }// class end

    /**
     * 能够执行下拉刷新的控制器。
     * <p/>
     * 下拉刷新只有两个步骤：
     * 1.首次加载数据（可以是来自缓存也可以是来自网络，通过{@link #firstLoadData(boolean, String, Map, String, NetResultFactory)}
     * 中的第一个参数控制）；
     * 2.执行下拉刷新请求数据{@link #doRefresh(String, Map, String, NetResultFactory)}
     * <p/>
     */
    interface Presenter extends BasePresenter{

        /**
         * 不会触发自动下拉刷新
         *
         * @param readCache 是否读取缓存
         */
        void firstLoadData(boolean readCache, String url, Map<String,String> postData, String cancelTag,
                           NetResultFactory factory);

        /** 执行刷新请求 */
        void doRefresh(String url,Map<String,String> postData, String cancelTag,
                       NetResultFactory factory);

        void cancelRequest(String cancelTag);
    }// class end

}
