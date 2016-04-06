package com.diagramsf.net;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** 执行网络请求，并传递结果 ，包含五中请求类型{@link Type} */
public interface NetRequest {

    /**
     * 只读取缓存，不考虑缓存是否过期，如果读取不到缓存也会 回调{@link NetRequestCallback#onSucceed(NetResult)}
     * ，只是结果传递的是null
     */
    int ONLY_CACHE = 1;
    /** 只读取网络，并且不会把结果缓存起来 */
    int ONLY_NET_NO_CACHE = 2;
    /** 忽略缓存，只读取网络，但是会把结果缓存起来 */
    int ONLY_NET_THEN_CACHE = 3;
    /** 如果有缓存就读取缓存(并且缓存不能过期（根据http报头信息判断是否过期的）)，否则读取网络 */
    int HTTP_HEADER_CACHE = 4;
    /** 优先读取缓存（不考虑缓存是否过期），如果没有缓存就读取网络（有缓存了就不读取网络了） */
    int PRIORITY_CACHE = 5;

    //这里做一下解释
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ONLY_CACHE, ONLY_NET_NO_CACHE, ONLY_NET_THEN_CACHE, HTTP_HEADER_CACHE, PRIORITY_CACHE})
    @interface Type {}

    /** 执行请求 */
    void doRequest(@Type int type);

    /** 此值会传递给结果 通过调用{@link NetResult#getRequestDeliveredTag()} 来获取 */
    void setDeliverToResultTag(Object tag);

    /** 设置请求完的回调接口 */
    void setCallBack(NetRequestCallback callback);

    /** 设置请求的缓存key */
    void setCacheKey(String cacheKey);

    /** 网络请求结果的回调接口 */
    interface NetRequestCallback {

        /** 网络请求成功回调 */
        void onSucceed(NetResult result);

        /** 网络请求失败回调 */
        void onFailed(NetFailedResult failResult);
    }

}
