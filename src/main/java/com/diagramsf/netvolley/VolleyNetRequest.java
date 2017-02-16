/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.diagramsf.netvolley;

import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.diagramsf.util.AppLog;
import com.diagramsf.util.StringUtil;
import com.diagramsf.net.NetContract;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * A request for retrieving a {@link NetContract.Result} response body at a given URL,
 * allowing for an optional {@link NetContract.Result} to be passed in as part of the
 * request body.
 */
public class VolleyNetRequest<T extends NetContract.Result> extends Request<T>
    implements NetContract.Request<T> {
  public static final String TAG = "VolleyNetRequest";

  /** 请求的编码格式. */
  protected static final String PROTOCOL_CHARSET = "utf-8";

  /**
   * 请求结果的内容类型，{@link com.android.volley.toolbox.JsonRequest}的请求类型是：
   * "application/json; charset=utf-8"
   * <p>
   * Content type for request.
   */
  protected static final String PROTOCOL_CONTENT_TYPE =
      String.format("application/x-www-form-urlencoded; charset=%s", PROTOCOL_CHARSET);

  private String mCacheKey;

  private ResultFactory<T> mResultFactory;

  private Map<String, String> mParams;
  private String mRequestBody;

  private Map<String, String> mHeader;

  private Priority mPriority;

  NetContract.ErrorListener mErrorListener;
  NetContract.Listener<T> mListener;

  /**
   * Creates a new request.
   *
   * @param method 请求方式
   * @param url 请求的URL
   * @param strRequest post数据
   * @param header 包头信息
   * @param priority 优先级
   * @param resultFactory 解析JSON数据，生成结果对象的工厂
   * @param errorListener 传递异常信息的接口
   */
  public VolleyNetRequest(int method, String url, String strRequest, Map<String, String> header,
      Request.Priority priority, ResultFactory<T> resultFactory,
      Response.ErrorListener errorListener) {
    super(method, url, errorListener);
    mResultFactory = resultFactory;
    mRequestBody = strRequest;
    mHeader = header;
    mPriority = priority;
  }

  /**
   * Creates a new request.
   *
   * @param method 请求方式
   * @param url 请求的URL
   * @param params post请求发送的数据
   * @param header 包头信息
   * @param priority 优先级
   * @param resultFactory 解析JSON数据，生成结果对象的工厂
   * @param errorListener 传递异常信息的接口
   */
  public VolleyNetRequest(int method, String url, Map<String, String> params,
      Map<String, String> header, Request.Priority priority, ResultFactory<T> resultFactory,
      Response.ErrorListener errorListener) {
    super(method, url, errorListener);
    mResultFactory = resultFactory;
    mParams = params;
    mHeader = header;
    mPriority = priority;
  }

  @Override protected void deliverResponse(T response) {
    if (null != mListener) {
      response.setRequestTag(mDeliverToResultTag);
      mListener.onSucceed(response);
    }
  }

  @Override public void deliverError(VolleyError error) {
    if (null != mErrorListener) {
      NetContract.Fail fr = new CommFail();
      fr.setDeliverToResultTag(mDeliverToResultTag);
      fr.setException(error);
      mErrorListener.onFailed(fr);
    }
  }

  /**
   * @deprecated Use {@link #getBodyContentType()}.
   */
  @Override public String getPostBodyContentType() {
    return getBodyContentType();
  }

  /**
   * @deprecated Use {@link #getBody()}.
   */
  @Override public byte[] getPostBody() throws AuthFailureError {
    return getBody();
  }

  //获取Param参数的编码格式
  @Override protected String getParamsEncoding() {
    return PROTOCOL_CHARSET;
  }

  //获取请求结果的内容类型
  @Override public String getBodyContentType() {
    return PROTOCOL_CONTENT_TYPE;
  }

  @Override public byte[] getBody() throws AuthFailureError {
    if (!StringUtil.isEmpty(mRequestBody)) {//如果是字符串型的post参数
      try {
        return mRequestBody.getBytes(PROTOCOL_CHARSET);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        return null;
      }
    } else {//此时根据Map类型的post参数来获取
      return super.getBody();
    }
  }

  //获取报头信息
  @Override public Map<String, String> getHeaders() throws AuthFailureError {
    if (null != mHeader) {
      return mHeader;
    }
    return super.getHeaders();
  }

  @Override public Priority getPriority() {
    if (null != mPriority) {
      return mPriority;
    }
    return super.getPriority();
  }

  //获取Post参数
  @Override protected Map<String, String> getParams() throws AuthFailureError {
    return mParams;
  }

  //注意：有一种情况会不走此方法直接回传结果的！
  //当只请求缓存，并且没有读取到缓存的时候，会在 CacheDispatch中直接返回结果给回调接口。
  @Override protected Response<T> parseNetworkResponse(NetworkResponse response) {
    try {
      NetContract.Result.ResultType type = createAppResultType(response);

      if (null == mResultFactory) {
        return Response.success(null, null);
      }
      T resultBean = mResultFactory.analysisResult(response.data, response.headers);
      if (null == resultBean) {//不需要转换成结果对象，表明此次请求不关注返回的结果，这里直接返回成功，并且结果回调null
        return Response.success(null, null);
      }

      resultBean.setResultType(type);

      Response<T> resultResponse =
          Response.success(resultBean, HttpHeaderParser.parseCacheHeaders(response));
      resultResponse.setResultDatLegitimacy(resultBean.checkResultLegitimacy());
      return resultResponse;
    } catch (Exception e) {
      e.printStackTrace();
      AppLog.e(TAG, e.toString());
      return Response.error(new ParseError(e));
    }
  }

  @Override public String getCacheKey() {
    if (StringUtil.isEmpty(mCacheKey)) {//如果没有额外设置缓存key
      String postParam = mRequestBody;//首先取字符串形式的post参数
      if (StringUtil.isEmpty(postParam)) {//如果没有获取到字符串参数，就获取Map类型的Post参数
        byte[] body = null;
        try {
          body = getBody();
        } catch (AuthFailureError authFailureError) {
          authFailureError.printStackTrace();
        }
        if (null != body) {
          try {
            postParam = new String(body, PROTOCOL_CHARSET);
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }
        }
      }

      if (StringUtil.isEmpty(postParam)) {
        postParam = "";
      }
      return getUrl() + postParam;
    } else {
      return mCacheKey;
    }
  }

  /** 获取请求结果是来自网络还是来自缓存 */
  private NetContract.Result.ResultType createAppResultType(NetworkResponse response) {
    boolean isFromCache = response.isFromCache();
    if (isFromCache) {
      AppLog.i(TAG, "结果从缓存来：" + getCacheKey());
      return NetContract.Result.ResultType.CATCH;
    } else {
      AppLog.i(TAG, "结果从网络来：" + getCacheKey());
      return NetContract.Result.ResultType.NET;
    }
  }

  //========================NetContract.NetRequest 接口方法
  private Object mDeliverToResultTag;

  @Override public void setDeliverToResultTag(Object tag) {
    mDeliverToResultTag = tag;
  }

  @Override public void setErrorListener(NetContract.ErrorListener errorListener) {
    mErrorListener = errorListener;
  }

  @Override public void setListener(NetContract.Listener<T> listener) {
    mListener = listener;
  }

  @Override public void setCacheKey(String cacheKey) {
    mCacheKey = cacheKey;
  }

  @Override public void request(@NetContract.Type int type) {
    switch (type) {
      case NetContract.ONLY_CACHE:
        setJustReadCache();
        break;
      case NetContract.ONLY_NET_NO_CACHE:
        setShouldCache(false);
        break;
      case NetContract.ONLY_NET_THEN_CACHE:
        skipCache();
        break;
      case NetContract.HTTP_HEADER_CACHE:
        //Volley默认就是此种方式请求的网络数据
        break;
      case NetContract.PRIORITY_CACHE:
        setReadCacheWithoutTimeLimit();
        break;
    }
  }
}
