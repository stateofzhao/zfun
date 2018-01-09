package com.diagramsf.core.netvolley.simple;

import com.diagramsf.core.learn.mvp.BasePresenter;
import com.diagramsf.core.learn.mvp.BaseView;
import com.diagramsf.core.netvolley.JsonToClassRequest;
import java.util.Map;

/**
 * Created by Diagrams on 2016/4/21 10:48
 */
public interface SimpleContract {

  interface View<M, E> extends BaseView<Presenter> {
    /** 显示 加载缓存时的进度条 */
    void onShowCacheLoadProgress();

    /** 隐藏 加载缓存时的进度条 */
    void onHideCacheLoadProgress();

    /** 显示 缓存加载结果 */
    void onShowCacheResult(M result);

    /** 没有读取到缓存（没有缓存） */
    void onShowNoCache();

    /** 读取缓存失败 */
    void onShowCacheFail(E result);

    //----------------------------------------------

    /** 显示 加载网络数据的进度条 */
    void onShowNetProgress();

    /** 隐藏 加载网络数据的进度条 */
    void onHideNetProgress();

    /** 显示 网络加载结果 */
    void onShowNetResult(M result);

    /** 显示 网络加载失败结果 */
    void onShowNetFail(E result);
  }//class end

  interface Presenter extends BasePresenter {
    /** 执行缓存请求， 不会请求网络 */
    void requestCache(String url, Map<String, String> postData, String cancelTag,
        JsonToClassRequest.ResultFactory factory);

    /** 执行网络请求， 不会读取缓存 */
    void requestNet(String url, Map<String, String> postData, String cancelTag,
        JsonToClassRequest.ResultFactory factory, boolean saveCache);

    void cancelCacheRequest(String cancelTag);

    void cancelNetRequest(String cancelTag);
  }//class end

  class ResultBean {

  }
}
