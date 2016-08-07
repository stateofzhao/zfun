package com.diagramsf.net;

import android.content.Context;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 执行网络请求，并传递结果 ，包含五中请求类型{@link Type}
 * <p>
 * Created by Diagrams on 2016/4/20 17:17
 */
public interface NetContract {
  /**
   * 只读取缓存，不考虑缓存是否过期，如果读取不到缓存也会 回调{@link Listener#onSucceed(Result)}
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

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ ONLY_CACHE, ONLY_NET_NO_CACHE, ONLY_NET_THEN_CACHE, HTTP_HEADER_CACHE, PRIORITY_CACHE })
  @interface Type {
  }

  interface Request<T extends Result> {
    /**
     * 此值会传递给结果 通过调用{@link Result#getRequestTag()}
     * 或者{@link Fail#getRequestTag()} 来获取
     */
    void setDeliverToResultTag(Object tag);

    /** 失败回调接口 */
    void setErrorListener(ErrorListener errorListener);

    /** 成功回调接口 */
    void setListener(Listener<T> listener);

    /** 设置请求的缓存key */
    void setCacheKey(String cacheKey);

    /** 执行请求 */
    void request(@Type int type);
  }//end class NetRequest

  /** 失败回调接口 */
  interface ErrorListener {
    /** 网络请求失败回调 */
    void onFailed(Fail fail);
  }//end class NetResultErrorListener

  /** 成功回调接口 */
  interface Listener<T extends Result> {
    /** 网络请求成功回调 */
    void onSucceed(T result);
  }//end class NetResultListener

  /** 网络请求成功的结果 */
  interface Result {
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

    /**
     * 检测结果数据的合法性
     *
     * @return true 数据合法；false数据不合法
     */
    boolean checkResultLegitimacy();

    /** 设置数据来源 */
    void setResultType(ResultType resultType);

    /** 设置{@link Request#setDeliverToResultTag(Object)} 传递的tag */
    void setRequestTag(Object tag);

    /** 获得数据来源类型 */
    ResultType getResultType();

    /** 获取相应的 {@link Request#setDeliverToResultTag(Object tag)} 中设置的 tag */
    Object getRequestTag();
  }//class NetSuccessResult end

  /** 网络请求失败的结果 */
  interface Fail {

    /** 设置结果的异常 */
    void setException(Exception e);

    /** 获取异常 */
    Exception getException();

    /** 提示异常信息,这个是给用户看的 */
    void toastFailStr(Context context);

    /** 打印异常信息，这个是调试程序用的 */
    void logFailInfo(String tag);

    /** 获取异常信息的 文字描述 */
    String getInfoText(Context context);

    /** 获取通过 {@link Request#setDeliverToResultTag(Object)} 设置的值 */
    Object getRequestTag();

    void setDeliverToResultTag(Object tag);
  }// class NetFailResult end
}
