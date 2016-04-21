package com.diagramsf.net;

import android.content.Context;
import android.support.annotation.IntDef;
import com.diagramsf.exceptions.AppException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 执行网络请求，并传递结果 ，包含五中请求类型{@link Type}
 * <p/>
 * Created by Diagrams on 2016/4/20 17:17
 */
public interface NetRequest {

    /**
     * 只读取缓存，不考虑缓存是否过期，如果读取不到缓存也会 回调{@link NetResultCallback#onSucceed(NetSuccessResult)}
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

    /** 网络请求结果的回调接口 */
    interface NetResultCallback {

        /** 网络请求成功回调 */
        void onSucceed(NetSuccessResult result);

        /** 网络请求失败回调 */
        void onFailed(NetFailResult fail);
    }// class end

    /** 网络请求成功的结果 */
    interface NetSuccessResult {

        enum ResultType {
            /** 来自网络 */
            NET,
            /** 来自缓存 */
            CATCH,
            /**
             * 中间结果，首先取得缓存结果，再请求网络来获取结果
             */
            INTERMEDIATE
        }

        /** 设置数据来源 */
        void setResultType(ResultType resultType);

        /** 设置tag ,通过 {@link #getRequestDeliveredTag()}获取此值 */
        void setRequestDeliveredTag(Object tag);

        /** 获得数据来源类型 */
        ResultType getResultType();

        /** 获取相应的 {@link NetRequest#setDeliverToResultTag(Object tag)} 中设置的 tag */
        Object getRequestDeliveredTag();

        /**
         * 检测结果数据的合法性
         *
         * @return true 数据合法；false数据不合法
         */
        boolean checkResultLegitimacy();

    }//class end

    /** 网络请求失败的结果 */
    interface NetFailResult {

        /** 设置结果的异常 */
        void setException(AppException e);

        /** 提示异常信息,这个是给用户看的 */
        void toastFailStr(Context context);

        /** 打印异常信息，这个是调试程序用的 */
        void logFailInfo(String tag);

        /** 获取异常信息的 文字描述 */
        String getInfoText(Context context);

        /** 获取通过 {@link NetRequest#setDeliverToResultTag(Object)} 设置的值 */
        Object getRequestDeliveredTag();

    }// class end


    /** 执行请求 */
    void doRequest(@Type int type, Object cancelTag);

    /**
     * 此值会传递给结果 通过调用{@link NetSuccessResult#getRequestDeliveredTag()}
     * 或者{@link NetFailResult#getRequestDeliveredTag()} 来获取
     */
    void setDeliverToResultTag(Object tag);

    /** 设置请求完的回调接口 */
    void setResultCallBack(NetResultCallback callback);

    /** 设置请求的缓存key */
    void setCacheKey(String cacheKey);

    /** 取消请求 */
    void cancelRequest(Object cancelTag);

}
