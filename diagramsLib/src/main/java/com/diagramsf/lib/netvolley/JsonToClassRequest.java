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

package com.diagramsf.lib.netvolley;

import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;

import com.diagramsf.lib.util.AppLog;
import com.diagramsf.lib.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * 能够使用URL请求服务器JSON数据，并且将JSON数据转换成Class对象。
 */
public class JsonToClassRequest<T> extends Request<T> {
  public static final String TAG = "JsonToClassRequest";

  /**
   * 只读取缓存，不考虑缓存是否过期，如果读取不到缓存也会 回调{@link Response.Listener#onResponse(Object)}
   * ，只是结果传递的是null
   */
  public static final int ONLY_CACHE = 1;
  /** 只读取网络，并且不会把结果缓存起来 */
  public static final int ONLY_NET_NO_CACHE = 2;
  /** 忽略缓存，只读取网络，但是会把结果缓存起来 */
  public static final int ONLY_NET_THEN_CACHE = 3;
  /** 如果有缓存就读取缓存(并且缓存不能过期（根据http报头信息判断是否过期的）)，否则读取网络 */
  public static final int HTTP_HEADER_CACHE = 4;
  /** 优先读取缓存（不考虑缓存是否过期），如果没有缓存就读取网络（有缓存了就不读取网络了） */
  public static final int PRIORITY_CACHE = 5;

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

  private Map<String, String> mParams;
  private String mRequestBody;

  private Map<String, String> mHeader;

  private Priority mPriority;

  private Class<T> mTargetClass;
  private ResultFactory<T> mResultFactory;
  private Response.ErrorListener mErrorListener;
  private Response.Listener<T> mListener;

  /**
   * 对服务器返回的结果进行解析
   */
  public interface ResultFactory<T> {

    /**
     * 对服务器返回的字符串 进行操作
     * e
     *
     * @param result 服务器返回的结果
     * @param responseHeader 服务器返回结果的header
     * @return 返回操作完的结果
     */
    T analysisResult(byte[] result, Map<String, String> responseHeader, boolean fromCache,
        String cacheKey) throws Exception;
  }

  /**
   * Creates a new request.
   *
   * @param method 请求方式
   * @param url 请求的URL
   * @param strRequest post数据
   * @param header 包头信息
   * @param priority 优先级
   * @param resultFactory 解析JSON数据，生成结果对象的工厂
   */
  public JsonToClassRequest(int method, String url, String strRequest, Map<String, String> header,
      Request.Priority priority, ResultFactory<T> resultFactory) {
    super(method, url, null);
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
   */
  public JsonToClassRequest(int method, String url, Map<String, String> params,
      Map<String, String> header, Request.Priority priority, ResultFactory<T> resultFactory) {
    super(method, url, null);
    mResultFactory = resultFactory;
    mParams = params;
    mHeader = header;
    mPriority = priority;
  }

  /**
   * Creates a new request.
   *
   * @param method 请求方式
   * @param url 请求的URL
   * @param strRequest post数据
   * @param header 包头信息
   * @param priority 优先级
   * @param clazz json要转换成的目标对象
   */
  public JsonToClassRequest(int method, String url, String strRequest, Map<String, String> header,
      Request.Priority priority, Class<T> clazz) {
    super(method, url, null);
    mRequestBody = strRequest;
    mHeader = header;
    mPriority = priority;
    mTargetClass = clazz;
    mResultFactory = null;
  }

  /**
   * Creates a new request.
   *
   * @param method 请求方式
   * @param url 请求的URL
   * @param params post请求发送的数据
   * @param header 包头信息
   * @param priority 优先级
   * @param clazz json要转换成的目标对象
   */
  public JsonToClassRequest(int method, String url, Map<String, String> params,
      Map<String, String> header, Request.Priority priority, Class<T> clazz) {
    super(method, url, null);
    mParams = params;
    mHeader = header;
    mPriority = priority;
    mResultFactory = null;
    mTargetClass = clazz;
  }

  @Override protected void deliverResponse(T response) {
    if (null != mListener) {
      mListener.onResponse(response);
    }
  }

  @Override public void deliverError(VolleyError error) {
    if (null != mErrorListener) {
      mErrorListener.onErrorResponse(error);
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
      if (null == mResultFactory) {
        return Response.success(null, null);
      }

      T result = createTargetModel(response);
      if (null == result) {//不需要转换成结果对象，表明此次请求不关注返回的结果，这里直接返回成功，并且结果回调null
        return Response.success(null, null);
      }

      return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));
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

  public void setErrorListener(Response.ErrorListener errorListener) {
    mErrorListener = errorListener;
  }

  public void setListener(Response.Listener<T> listener) {
    mListener = listener;
  }

  public void setCacheKey(String cacheKey) {
    mCacheKey = cacheKey;
  }

  public void requestType(int type) {
    switch (type) {
      case ONLY_CACHE:
        setJustReadCache();
        break;
      case ONLY_NET_NO_CACHE:
        setShouldCache(false);
        break;
      case ONLY_NET_THEN_CACHE:
        skipCache();
        break;
      case PRIORITY_CACHE:
        setReadCacheWithoutTimeLimit();
        break;
      case HTTP_HEADER_CACHE:
        //Volley默认就是此种方式请求的网络数据
        break;
      default:
        break;
    }
  }

  private T createTargetModel(NetworkResponse response) throws Exception {
    if (null != mResultFactory) {
      return mResultFactory.analysisResult(response.data, response.headers, response.isFromCache(),
          getCacheKey());
    }

    if (null != mTargetClass) {
      String json = new String(response.data, PROTOCOL_CHARSET);
      JsonReader reader = new JsonReader(new StringReader(json));
      JsonParser parser = new JsonParser();
      JsonElement element = parser.parse(reader);
      return new Gson().fromJson(element, mTargetClass);
    }
    return null;
  }
}
