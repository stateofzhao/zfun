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

package com.android.volley;

/**
 * 封装一个交付的解析Response, 其实Response只是一个代理类，并不是真正的结果数据类，真正的数据结果是 T ，它起到一个中间处理结果T
 * 的逻辑层. 具体缓存网络请求数据（头信息和元数据 byte[]）的是 {@link NetworkResponse}(跟Response一点关系都没有)
 * 和 {@link Cache.Entry}. </p> 包括以下功能： 1.内部定义一个 错误接口,并且持有一个错误接口变量，用来传递异常信息，</p>
 * 2.内部定义一个 结果接口{@link Listener}，但是没有持有，一般在Request子类中持有这个接口的变量，来传递 Respose。</p>
 * 3.维护一个 Cache.Entry 高速缓存（根据服务器响应 header 决定是启用），</p> 4.维护一个T result，这个result
 * 就是自定义的 需要 通过 结果接口{@link Listener} 传递回去的</p> 5. </p>
 * <p/>
 * <p/>
 * Encapsulates a parsed response for delivery.
 *
 * @param <T> Parsed type of this response
 */
public class Response<T> {

    /** Callback interface for delivering parsed responses. </p>传递结果的接口 */
    public interface Listener<T> {
        /** Called when a response is received.
         * </p>
         * 当Response接收到后回调这个方法 .
         * </p>
         * <b>注意此处的response有可能会是null（只请求缓存时如果
         * 缓存不存在的话，此处就是null），注意判断</b>*/
        public void onResponse(T response);
    }

    /**
     * Callback interface for delivering error responses. </p>回调接口，传递 error
     * response
     */
    public interface ErrorListener {
        /**
         * Callback method that an error has been occurred with the provided
         * error code and optional user-readable message.
         * <p/>
         * </p>
         * <p/>
         * 回调方法，当异常发生时，提供错误码和 用户可读的 信息。
         */
        public void onErrorResponse(VolleyError error);
    }

    /**
     * Returns a successful response containing the parsed result. </p>返回一个成功的
     * response包含解析的结果
     */
    public static <T> Response<T> success(T result, Cache.Entry cacheEntry) {
        return new Response<T>(result, cacheEntry);
    }

    /**
     * Returns a failed response containing the given error code and an optional
     * localized message displayed to the user.
     */
    public static <T> Response<T> error(VolleyError error) {
        return new Response<T>(error);
    }

    /** Parsed response, or null in the case of error. */
    public final T result;

    /** Cache metadata for this response, or null in the case of error. */
    public final Cache.Entry cacheEntry;

    /** Detailed error information if <code>errorCode != OK</code>. */
    public final VolleyError error;

    /**
     * 标记是否为中间态，如果是true，表示我们读取到了缓存结果，但是缓存结果过期网络，此时我们会把
     * 缓存结果传递出去，然后接着去请求网络，然后再把网络结果传递出去。
     * <p/>
     * True if this response was a soft-expired one and a second one MAY be
     * coming.
     */
    public boolean intermediate = false;

    /**
     * Returns whether this response is considered successful.
     */
    public boolean isSuccess() {
        return error == null;
    }

    private Response(T result, Cache.Entry cacheEntry) {
        this.result = result;
        this.cacheEntry = cacheEntry;
        this.error = null;
    }

    private Response(VolleyError error) {
        this.result = null;
        this.cacheEntry = null;
        this.error = error;
    }
}
