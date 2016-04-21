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

package com.diagramsf.netvolley.volleyrequest;


import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.diagramsf.helpers.AppDebugLog;
import com.diagramsf.helpers.StringUtils;
import com.diagramsf.net.NetRequest;
import com.diagramsf.netvolley.NetResultFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * A request for retrieving a {@link NetRequest.NetSuccessResult} response body at a given URL,
 * allowing for an optional {@link NetRequest.NetSuccessResult} to be passed in as part of the
 * request body.
 */
public class VolleyNetRequest extends Request<NetRequest.NetSuccessResult> {

    public static final String TAG = "VolleyNetRequest";

    /** 请求的编码格式. */
    protected static final String PROTOCOL_CHARSET = "utf-8";

    /**
     * 请求结果的内容类型，{@link com.android.volley.toolbox.JsonRequest}的请求类型是：
     * "application/json; charset=utf-8"
     * <p/>
     * Content type for request.
     */
    protected static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/x-www-form-urlencoded; charset=%s", PROTOCOL_CHARSET);

    private final Response.Listener<NetRequest.NetSuccessResult> mListener;

    private String mCacheKey;

    private NetResultFactory mResultFactory;

    private Map<String, String> mParams;
    private String mRequestBody;


    /**
     * Creates a new request.
     *
     * @param method        请求方式
     * @param url           请求的URL
     * @param strRequest    post请求发送的数据
     * @param resultFactory 解析JSON数据，生成结果对象的工厂
     * @param listener      传递正确结果的接口
     * @param errorListener 传递异常信息的接口
     */
    public VolleyNetRequest(int method, String url, String strRequest,
                            NetResultFactory resultFactory,
                            Response.Listener<NetRequest.NetSuccessResult> listener,
                            Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mResultFactory = resultFactory;
        mRequestBody = strRequest;
        mListener = listener;
    }

    /**
     * Creates a new request.
     *
     * @param method        请求方式
     * @param url           请求的URL
     * @param params        post请求发送的数据
     * @param resultFactory 解析JSON数据，生成结果对象的工厂
     * @param listener      传递正确结果的接口
     * @param errorListener 传递异常信息的接口
     */
    public VolleyNetRequest(int method, String url, Map<String, String> params,
                            NetResultFactory resultFactory,
                            Response.Listener<NetRequest.NetSuccessResult> listener,
                            Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        mResultFactory = resultFactory;
        mParams = params;
        mListener = listener;
    }

    @Override
    protected void deliverResponse(NetRequest.NetSuccessResult response) {
        mListener.onResponse(response);
    }

    /**
     * @deprecated Use {@link #getBodyContentType()}.
     */
    @Override
    public String getPostBodyContentType() {
        return getBodyContentType();
    }

    /**
     * @deprecated Use {@link #getBody()}.
     */
    @Override
    public byte[] getPostBody() throws AuthFailureError {
        return getBody();
    }

    //获取Param参数的编码格式
    @Override
    protected String getParamsEncoding() {
        return PROTOCOL_CHARSET;
    }

    //获取请求结果的内容类型
    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (!StringUtils.isEmpty(mRequestBody)) {//如果是字符串型的post参数
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
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> head = new HashMap<>();
        head.put("User-Agent", Volley.userAgent);
        return head;
    }

    //获取Post参数
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    //注意：有一种情况会不走此方法直接回传结果的！
    //当只请求缓存，并且没有读取到缓存的时候，会在 CacheDispatch中直接返回结果给回调接口。
    @Override
    protected Response<NetRequest.NetSuccessResult> parseNetworkResponse(NetworkResponse response) {
        try {

            NetRequest.NetSuccessResult.ResultType type = createAppResultType(response);

            if (null == mResultFactory) {
                return Response.success(null, null);
            }
            NetRequest.NetSuccessResult resultBean = mResultFactory.analysisResult(response.data,
                    response.headers);
            if (null == resultBean) {//不需要转换成结果对象，表明此次请求不关注返回的结果，这里直接返回成功，并且结果回调null
                return Response.success(null, null);
            }

            resultBean.setResultType(type);

            Response<NetRequest.NetSuccessResult> resultResponse = Response.success(resultBean,
                    HttpHeaderParser.parseCacheHeaders(response));
            resultResponse.setResultDatLegitimacy(resultBean
                    .checkResultLegitimacy());

            return resultResponse;
        } catch (Exception e) {
            e.printStackTrace();
            AppDebugLog.e(TAG, e.toString());
            return Response.error(new ParseError(e));
        }
    }

    /** 必须去掉 接口版本号字段，否则永远读取不到缓存，因为当接口请求成功后会更新本地保存的接口版本号，这样当再次请求时，CacheKey就变了 */
    @Override
    public String getCacheKey() {
        if (StringUtils.isEmpty(mCacheKey)) {//如果没有额外设置缓存key

            String postParam = mRequestBody;//首先取字符串形式的post参数
            if (StringUtils.isEmpty(postParam)) {//如果没有获取到字符串参数，就获取Map类型的Post参数
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

            if (StringUtils.isEmpty(postParam)) {
                postParam = "";
            }
            return getUrl() + postParam;
        } else {
            return mCacheKey;
        }
    }

    //获取结果缓存到本地的键名

    final public void setCacheKey(String cacheKey) {
        mCacheKey = cacheKey;
    }

    /** 获取请求结果是来自网络还是来自缓存 */
    private NetRequest.NetSuccessResult.ResultType createAppResultType(NetworkResponse response) {
        boolean isFromCache = response.isFromCache();
        if (isFromCache) {
            AppDebugLog.i(TAG, "结果从缓存来：" + getCacheKey());
            return NetRequest.NetSuccessResult.ResultType.CATCH;
        } else {
            AppDebugLog.i(TAG, "结果从网络来：" + getCacheKey());
            return NetRequest.NetSuccessResult.ResultType.NET;
        }
    }

}
