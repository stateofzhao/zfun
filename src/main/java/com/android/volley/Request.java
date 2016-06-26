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

import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import com.android.volley.VolleyLog.MarkerLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

/**
 * 所有网络请求的基类。 T 代表与 Request 绑定的 Response。
 * <p>
 * <p/>
 * 包含的功能有：</p>
 * 1.请求类型（POST,GET,DELETE,PUT,HEAD,OPTIONS,TRACE,PATCH）；</p>
 * 2.封装请求的URL；</p>
 * 3.请求信息的编码；</p>
 * 4.调试信息；</p>
 * 5.错误监听接口；</p>
 * 6.请求序列号，用户执行先进先出顺序；</p>
 * 7.请求标识TAG，用来取消请求用的；</p>
 * 8.引用 {@link RequestQueue}队列，用来通知队列请求结束</p>
 * 9. 持有{@link Cache.Entry} 高速缓存</p>
 * 10.是否需要缓存的配置变量</p>
 * 11.注意Request基类并不持有 PostParameters和PostBody，需要子类看情况重写getBody(),getPostBody()或者 getParameters()方法 </p>
 * 12. 注意，Request基类 也不持有 请求Headers(请求报头信息),具体需要子类重写getHeaders()方法 </p>
 * 13.维护一个boolean变量，标记是否已经传递了Response</p>
 * 14.维护一个Boolean变量，标记是否缓存 Response</p>
 * 15.子类需要重写的方法有{@link #deliverResponse(Object)}和{@link #parseNetworkResponse(NetworkResponse)}，{@link #parseNetworkError(VolleyError)}
 * 这个方法根据需要重写，也可以不重写直接把 VolleyError返回处理，重写的话可以归类Exception。
 * <p>
 * Base class for all network requests.
 *
 * @param <T> The type of parsed response this request expects.
 */
public abstract class Request<T> implements Comparable<Request<T>> {

    /**
     * Default encoding for POST or PUT parameters. See
     * {@link #getParamsEncoding()}.
     */
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    /**
     * 支持的 request 方法。
     * <p>
     * <p>
     * Supported request methods.
     */
    public interface Method {
        int DEPRECATED_GET_OR_POST = -1;
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

    /** An event log tracing the lifetime of this request; for debugging. */
    private final MarkerLog mEventLog = MarkerLog.ENABLED ? new MarkerLog()
            : null;

    /**
     * Request method of this request. Currently supports GET, POST, PUT,
     * DELETE, HEAD, OPTIONS, TRACE, and PATCH.
     */
    private final int mMethod;

    /** URL of this request. */
    private final String mUrl;

    /**
     * {@link TrafficStats}的默认 tag。（TrafficStats 是对网络连接打tag 用的。 DDMS的 Network Traffic Tool 工具能够
     * 实时地监测网络的使用情况，如果需要查看每一个网络连接的情况，就需要TrafficStats 对网络连接打tag了）。
     * 如果要更加清楚地看清每一个网络连接的使用情况可以在程序中对网络连接打tag,如对socket连接可以这样：
     * TrafficStats.setThreadStatsTag(0xF00D);
     * TrafficStats.tagSocket(outputSocket);
     * // Transfer data using socket
     * TrafficStats.untagSocket(outputSocket);
     * <p>
     * 对于Apache HttpClient and URLConnection 会自动打上tag，所以只要设置上tag名就可以了：
     * TrafficStats.setThreadStatsTag(0xF00D);
     * try {
     * // Make network request using HttpClient.execute()
     * } finally {
     * TrafficStats.clearThreadStatsTag();
     * }
     * <p>
     * <p>
     * Default tag for {@link TrafficStats}.
     */
    private final int mDefaultTrafficStatsTag;

    /**
     * 错误监听接口
     * <p>
     * Listener interface for errors.
     */
    private final Response.ErrorListener mErrorListener;

    /**
     * 此请求的序列号,用于执行先进先出顺序。
     * <p>
     * Sequence number of this request, used to enforce FIFO ordering.
     */
    private Integer mSequence;

    /** The request queue this request is associated with. */
    private RequestQueue mRequestQueue;

    /** Whether or not responses to this request should be cached. */
    private boolean mShouldCache = true;

    /** Whether or not this request has been canceled. */
    private boolean mCanceled = false;

    /** Whether or not a response has been delivered for this request yet. */
    private boolean mResponseDelivered = false;

    // A cheap variant of request tracing used to dump slow
    // requests.轻量级的变量，用来跟踪 requests 速度。调试用的
    private long mRequestBirthTime = 0;

    /**
     * log请求的阀值(即使不启用调试日志记录)。
     * <p>
     * <p>
     * Threshold at which we should log the request (even when debug logging is
     * not enabled).
     */
    private static final long SLOW_REQUEST_THRESHOLD_MS = 3000;

    /** 为这个请求重试策略。<p> The retry policy for this request. */
    private RetryPolicy mRetryPolicy;

    /**
     * When a request can be retrieved from cache but must be refreshed from the
     * network, the cache entry will be stored here so that in the event of a
     * "Not Modified" response, we can be sure it hasn't been evicted from
     * cache.
     */
    private Cache.Entry mCacheEntry = null;

    /** An opaque token tagging this request; used for bulk cancellation. */
    private Object mTag;

    /**
     * Creates a new request with the given URL and error listener. Note that
     * the normal response listener is not provided here as delivery of
     * responses is provided by subclasses, who have a better idea of how to
     * deliver an already-parsed response.
     *
     * @deprecated Use
     * {@link #Request(int, String, Response.ErrorListener)}
     * .
     */
    @Deprecated
    public Request(String url, Response.ErrorListener listener) {
        this(Method.DEPRECATED_GET_OR_POST, url, listener);
    }

    /**
     * 使用给定的值，创建一个Request。
     * 注意：本类没有提供 normal response listener（传递 response用的），
     * 本类的子类提供了高效聪明的 response listener传递已经解析了的response。
     * <p>
     * <p>
     * Creates a new request with the given method (one of the values from
     * {@link Method}), URL, and error listener. Note that the normal response
     * listener is not provided here as delivery of responses is provided by
     * subclasses, who have a better idea of how to deliver an already-parsed
     * response.
     */
    public Request(int method, String url, Response.ErrorListener listener) {
        mMethod = method;
        mUrl = url;
        mErrorListener = listener;
        setRetryPolicy(new DefaultRetryPolicy());

        mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }

    /**
     * 获取 本request的 method(是{@link Method}的一个值)。
     * <p>
     * <p>
     * Return the method for this request. Can be one of the values in
     * {@link Method}.
     */
    public int getMethod() {
        return mMethod;
    }

    /**
     * 给Request设置 tag，用来执行 取消 所有 requests 用的{@link RequestQueue#cancelAll(Object)}。
     * <p>
     * <p>
     * Set a tag on this request. Can be used to cancelRequest all requests with this
     * tag by {@link RequestQueue#cancelAll(Object)}.
     *
     * @return This Request object to allow for chaining.
     */
    public Request<?> setTag(Object tag) {
        mTag = tag;
        return this;
    }

    /**
     * 获取request的tag。
     * <p>
     * <p>
     * Returns this request's tag.
     *
     * @see Request#setTag(Object)
     */
    public Object getTag() {
        return mTag;
    }

    /**
     * 用在 {@link TrafficStats#setThreadStatsTag(int)}方法上,设置tag名。
     * <p>
     *
     * @return A tag for use with {@link TrafficStats#setThreadStatsTag(int)}
     */
    public int getTrafficStatsTag() {
        return mDefaultTrafficStatsTag;
    }

    /**
     * 获取 URL 中host 部分的哈希码，如果没有的话返回0。
     * <p>
     *
     * @return The hashcode of the URL's host component, or 0 if there is none.
     */
    private static int findDefaultTrafficStatsTag(String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            if (uri != null) {
                String host = uri.getHost();//获取URL中的host，例如 http://www.baidu.com  host就是www.baidu.com
                if (host != null) {
                    return host.hashCode();
                }
            }
        }
        return 0;
    }

    /**
     * 设置重试策略。
     * <p>
     * <p>
     * <p>
     * Sets the retry policy for this request.
     *
     * @return This Request object to allow for chaining.
     */
    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        mRetryPolicy = retryPolicy;
        return this;
    }

    /**
     * 添加一个标记，打印本Request的运行信息。用来调试用的。如果调试信息不可用，就根据一个变量来检测 request的速度。
     * <p>
     * <p>
     * Adds an event to this request's event log; for debugging.
     */
    public void addMarker(String tag) {
        if (MarkerLog.ENABLED) {
            mEventLog.add(tag, Thread.currentThread().getId());
        } else if (mRequestBirthTime == 0) {
            mRequestBirthTime = SystemClock.elapsedRealtime();
        }
    }

    /**
     * 通知request 队列，这个request已经执行完毕（成功或者 有异常）。
     * <P>
     * <p/>
     * Notifies the request queue that this request has finished (successfully
     * or with error).
     * <p/>
     * <p>
     * Also dumps all events from this request's event log; for debugging.
     * </p>
     */
    void finish(final String tag) {
        if (mRequestQueue != null) {
            mRequestQueue.finish(this);//通知队列，本request结束了
        }
        if (MarkerLog.ENABLED) {
            final long threadId = Thread.currentThread().getId();
            if (Looper.myLooper() != Looper.getMainLooper()) {
                // If we finish marking off of the main thread, we need to
                // actually do it on the main thread to ensure correct ordering.
                Handler mainThread = new Handler(Looper.getMainLooper());
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        mEventLog.add(tag, threadId);
                        mEventLog.finish(this.toString());
                    }
                });
                return;
            }

            mEventLog.add(tag, threadId);
            mEventLog.finish(this.toString());
        } else {
            long requestTime = SystemClock.elapsedRealtime()
                    - mRequestBirthTime;
            if (requestTime >= SLOW_REQUEST_THRESHOLD_MS) { //如果请求大于 阀值，就打印log信息
                VolleyLog.d("%d ms: %s", requestTime, this.toString());
            }
        }
    }

    /**
     * 把request和给定的 request queue联系起来。当本request结束后，request queue会得到通知。
     * <p>
     * <p>
     * <p>
     * Associates this request with the given queue. The request queue will be
     * notified when this request has finished.
     *
     * @return This Request object to allow for chaining.
     */
    public Request<?> setRequestQueue(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
        return this;
    }

    /**
     * 给request设置序号，让{@link RequestQueue}使用的。
     * <p>
     * <p>
     * Sets the sequence number of this request. Used by {@link RequestQueue}.
     *
     * @return This Request object to allow for chaining.
     */
    public final Request<?> setSequence(int sequence) {
        mSequence = sequence;
        return this;
    }

    /**
     * 返回 request的序号。如果 sequence为null，抛出运行异常，表明在设置 sequence之前调用了 本方法。
     * <p>
     * Returns the sequence number of this request.
     */
    public final int getSequence() {
        if (mSequence == null) {
            throw new IllegalStateException(
                    "getSequence called before setSequence");
        }
        return mSequence;
    }

    /**
     * 返回 request的 URL。
     * <p>
     * <p>
     * Returns the URL of this request.
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * 返回 request的 缓存用的key。默认是URL。
     * <p>
     * <p>
     * Returns the cache key for this request. By default, this is the URL.
     */
    public String getCacheKey() {
        return getUrl();
    }

    /**
     * 使用 从缓存中检索出的条目注释 request。用于高速缓存的一致性。
     * <p>
     * <p>
     * <p>
     * Annotates this request with an entry retrieved for it from cache. Used
     * for cache coherency support.
     *
     * @return This Request object to allow for chaining.
     */
    public Request<?> setCacheEntry(Cache.Entry entry) {
        mCacheEntry = entry;
        return this;
    }

    /**
     * 返回带注释的缓存条目,如果没有就返回null。
     * <p>
     * <p>
     * Returns the annotated cache entry, or null if there isn't one.
     */
    public Cache.Entry getCacheEntry() {
        return mCacheEntry;
    }

    /**
     * 标记request 被 取消。没有回调将交付 。
     * <p>
     * <p>
     * Mark this request as canceled. No callback will be delivered.
     */
    public void cancel() {
        mCanceled = true;
    }

    /**
     * request是否被取消，如果取消返回true。
     * <p>
     * <p>
     * Returns true if this request has been canceled.
     */
    public boolean isCanceled() {
        return mCanceled;
    }


    /**
     * 其实 post parameters 就是post发送的数据 键值对数据，和 GET拼在URL后面的键值对参数一样。但是如果上传文件和数据一块发送的话，
     * 就不是这种形式发送键值对数据了，而是需要复杂的 表格 拼写。
     * <p>
     * 在Volley中 本类中实现是 以键值对的形式（ key1 = value1 & key2 = value2）发送post parameters的，
     * 如果需要 文件上传和parameter同时提交就需要自己重新 继承 Request 重写  getPostBody()和getBody()这两个方法！
     * 再修改{@link com.android.volley.toolbox.HurlStack#setConnectionParametersForRequest()}方法，
     * 添加一种 请求方式 UPLOAD。
     * <p>
     * 本基类中并没有 设置POST parameters的方法，也没有设置 PostBody的方法，所以需要在Request的子类中自己重写其中的一个方法使
     * 不是返回null（本基类默认 getPostParameters(),getParameters(),getPostBody()和getBody()方法都是返回null）。
     * <p>
     * 返回request的 Map类型的 POST paramsters，或者如果是简单的GET request返回null。
     * 可以抛出{@link AuthFailureError}作为验证可能被要求提供这些值。
     * <p>
     * Returns a Map of POST parameters to be used for this request, or null if
     * a simple GET should be used. Can throw {@link AuthFailureError} as
     * authentication may be required to provide these values.
     * <p>
     * Note that only one of getPostParams() and getPostBody() can return a
     * non-null value.
     *
     * @throws AuthFailureError In the event of auth failure
     * @deprecated Use {@link #getParams()} instead.
     */
    @Deprecated
    protected Map<String, String> getPostParams() throws AuthFailureError {
        return getParams();
    }

    /**
     * 返回POST paramsters的编码格式，对 {@link #getPostParams()}返回的原始POST body进行编码。
     * 返回值为格式为："UTF-8"等。
     * <p>
     * 返回的编码格式，同时控制两个部分：
     * <p>
     * <li>转换parameter names和values，当转换为字节之前使用URLEncode来编码 names和values</li>
     * <li>编码URL中的 parameters 到原始的raw byte 数组中</li>
     * <p>
     * <p>
     * Returns which encoding should be used when converting POST parameters
     * returned by {@link #getPostParams()} into a raw POST body.
     * <p>
     * <p>
     * This controls both encodings:
     * <ol>
     * <li>The string encoding used when converting parameter names and values
     * into bytes prior to URL encoding them.</li>
     * <li>The string encoding used when converting the URL encoded parameters
     * into a raw byte array.</li>
     * </ol>
     *
     * @deprecated Use {@link #getParamsEncoding()} instead.
     */
    @Deprecated
    protected String getPostParamsEncoding() {
        return getParamsEncoding();
    }

    /**
     * @deprecated Use {@link #getBodyContentType()} instead.
     */
    @Deprecated
    public String getPostBodyContentType() {
        return getBodyContentType();
    }

    /**
     * Returns the raw POST body to be sent.
     *
     * @throws AuthFailureError In the event of auth failure
     * @deprecated Use {@link #getBody()} instead.
     */
    @Deprecated
    public byte[] getPostBody() throws AuthFailureError {
        // Note: For compatibility with legacy clients of volley, this
        // implementation must remain
        // here instead of simply calling the getBody() function because this
        // function must
        // call getPostParams() and getPostParamsEncoding() since legacy clients
        // would have
        // overridden these two member functions for POST requests.
        Map<String, String> postParams = getPostParams();
        if (postParams != null && postParams.size() > 0) {
            return encodeParameters(postParams, getPostParamsEncoding());
        }
        return null;
    }

    /**
     * 返回本request的 额外的HTTP headers 集合，在验证失败的情况下能够抛出{@link AuthFailureError}，验证需要的提供的这些值 。
     * <p>
     * <p>
     * Returns a list of extra HTTP headers to go along with this request. Can
     * throw {@link AuthFailureError} as authentication may be required to
     * provide these values.
     *
     * @throws AuthFailureError In the event of auth failure
     */
    public Map<String, String> getHeaders() throws AuthFailureError {
        return Collections.emptyMap();
    }

    /**
     * 返回 POST或者PUT request的 Map格式的parameters,没有进行过编码。
     * <P>
     * 注意：应该重写这个方法来自定义 数据。
     * <P>
     * <p/>
     * Returns a Map of parameters to be used for a POST or PUT request. Can
     * throw {@link AuthFailureError} as authentication may be required to
     * provide these values.
     * <p/>
     * <p>
     * Note that you can directly override {@link #getBody()} for custom data.
     * </p>
     *
     * @throws AuthFailureError in the event of auth failure
     */
    protected Map<String, String> getParams() throws AuthFailureError {
        return null;
    }

    /**
     * 返回POST或者PUT的 paramsters的编码格式，对 {@link #getPostParams()}返回的原始POST或者PUT的 body进行编码。
     * 返回值为格式为："UTF-8"等。
     * <p>
     * 返回的编码格式，同时控制两个部分：
     * <p>
     * <li>转换parameter names和values，当转换为字节之前使用URLEncode来编码 names和values</li>
     * <li>转换URL中的 parameters 到原始的raw byte 数组中</li>
     * <p>
     * <p>
     * <p>
     * Returns which encoding should be used when converting POST or PUT
     * parameters returned by {@link #getParams()} into a raw POST or PUT body.
     * <p>
     * <p>
     * This controls both encodings:
     * <ol>
     * <li>The string encoding used when converting parameter names and values
     * into bytes prior to URL encoding them.</li>
     * <li>The string encoding used when converting the URL encoded parameters
     * into a raw byte array.</li>
     * </ol>
     */
    protected String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    /** 返回格式为 "application/x-www-form-urlencoded; charset=UTF-8" 等 */
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset="
                + getParamsEncoding();
    }

    /**
     * 返回发送的 POST或者PUT 的body 的原始数据（已经进行了转码）。
     * <p>
     * Returns the raw POST or PUT body to be sent.
     *
     * @throws AuthFailureError in the event of auth failure
     */
    public byte[] getBody() throws AuthFailureError {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded
     * encoded string.
     */
    protected final byte[] encodeParameters(Map<String, String> params,
                                            String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {

            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(),
                        paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(),
                        paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: "
                    + paramsEncoding, uee);
        }
    }

    /**
     * 设置是否需要缓存该 request的 responses。
     * <p>
     * Set whether or not responses to this request should be cached.
     *
     * @return This Request object to allow for chaining.
     */
    public final Request<?> setShouldCache(boolean shouldCache) {
        mShouldCache = shouldCache;
        return this;
    }

    /**
     * 如果这个request对应的response需要缓存返回 true。
     * <p>
     * Returns true if responses to this request should be cached.
     */
    public final boolean shouldCache() {
        return mShouldCache;
    }

    /**
     * 优先级值。请求将按照先入先出的顺序从较高的优先级处理，以较低的优先级。
     * <p>
     * Priority values. Requests will be processed from higher priorities to
     * lower priorities, in FIFO order.
     */
    public enum Priority {
        LOW, NORMAL, HIGH, IMMEDIATE
    }

    /**
     * 返回 request的优先级{@link Priority} ，默认是{@link Priority#NORMAL}。
     * <p>
     * Returns the {@link Priority} of this request; {@link Priority#NORMAL} by
     * default.
     */
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    /**
     * Returns the socket timeout in milliseconds per retry attempt. (This value
     * can be changed per retry attempt if a backoff is specified via
     * backoffTimeout()). If there are no retry attempts remaining, this will
     * cause delivery of a {@link TimeoutError} error.
     */
    public final int getTimeoutMs() {
        return mRetryPolicy.getCurrentTimeout();
    }

    /**
     * Returns the retry policy that should be used for this request.
     */
    public RetryPolicy getRetryPolicy() {
        return mRetryPolicy;
    }

    /**
     * 标记 request的 response 已经传递过了。
     * <p>
     * Mark this request as having a response delivered on it. This can be used
     * later in the request's lifetime for suppressing identical responses.
     */
    public void markDelivered() {
        mResponseDelivered = true;
    }

    /**
     * 如果 request 已经得到了 response 返回true。
     * <p>
     * Returns true if this request has had a response delivered for it.
     */
    public boolean hasHadResponseDelivered() {
        return mResponseDelivered;
    }

    /**
     * 子类必须重写这个方法，来解析原始的 network response，并返回返回相应的response类型。
     * 这个方法在worker thread 中调用。如果返回null，response 不会不传递。
     * <p>
     * Subclasses must implement this to parse the raw network response and
     * return an appropriate response type. This method will be called from a
     * worker thread. The response will not be delivered if you return null.
     *
     * @param response Response from the network ( 从network返回的 Response)
     *
     * @return The parsed response, or null in the case of an error
     */
    abstract protected Response<T> parseNetworkResponse(NetworkResponse response);

    /**
     * 子类能够重写这个方法 来解析  networkError 并返回一个具体的 error。
     * <P>
     * 默认实现，只是返回解析的 networkError。
     * <P>
     * Subclasses can override this method to parse 'networkError' and return a
     * more specific error.
     * <p>
     * The default implementation just returns the passed 'networkError'.
     * </p>
     *
     * @param volleyError the error retrieved from the network(从网络中检索到的错误)
     *
     * @return an NetworkError augmented with additional information(包含其他信息的NetworkError)
     */
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return volleyError;
    }

    /**
     * 子类必须重写这个方法，将 已经解析过的 response 传递给 它们的 listeners 。给定的响应是保证非空；
     * 解析失败的 responses 是不会被传递的。
     * <p>
     * <p>
     * <p>
     * Subclasses must implement this to perform delivery of the parsed response
     * to their listeners. The given response is guaranteed to be non-null;
     * responses that fail to parse are not delivered.
     *
     * @param response The parsed response returned by
     *                 {@link #parseNetworkResponse(NetworkResponse)}.
     *                 <p>
     *                 被{@link #parseNetworkResponse(NetworkResponse)}解析过的 response 。
     */
    abstract protected void deliverResponse(T response);

    /**
     * 传递 error 信息 给 ErrorListener（在 Request 初始化时设置的 ErrorListener）。
     * <p>
     * <p>
     * <p>
     * Delivers error message to the ErrorListener that the Request was
     * initialized with.
     *
     * @param error Error details
     */
    public void deliverError(VolleyError error) {
        if (mErrorListener != null) {
            mErrorListener.onErrorResponse(error);
        }
    }

    /**
     * 比较排序，从高优先级到低优先级，如果优先级相同再 按照先入先出规则排序。
     * <p>
     * <p>
     * <p>
     * Our comparator sorts from high to low priority, and secondarily by
     * sequence number to provide FIFO ordering.
     */
    @Override
    public int compareTo(Request<T> other) {
        Priority left = this.getPriority();
        Priority right = other.getPriority();

        // High-priority requests are "lesser" so they are sorted to the front.
        // Equal priorities are sorted by sequence number to provide FIFO
        // ordering.
        return left == right ? this.mSequence - other.mSequence : right
                .ordinal() - left.ordinal();
    }

    @Override
    public String toString() {
        String trafficStatsTag = "0x"
                + Integer.toHexString(getTrafficStatsTag());
        return (mCanceled ? "[X] " : "[ ] ") + getUrl() + " " + trafficStatsTag
                + " " + getPriority() + " " + mSequence;
    }

    //------------自己添加的
    //如果有缓存就直接读取缓存返回结果，并且不判断是否缓存超时（根据服务器返回的时间来判断的）。
    private boolean mReadCacheWithoutTimeLimit = false;
    //只读取缓存，读取不到缓存就返回 结果null
    private boolean mJustReadCache = false;


    /**
     * 是否忽略缓存过期时间,如果读取到了缓存就传递结果，结束；如果没有读取到就会读取网络
     *
     * @return true 忽略缓存过期时间；false不忽略
     */
    public boolean readCacheWithoutTimeLimit() {
        return mReadCacheWithoutTimeLimit;
    }

    /** 只读取缓存，读取不到缓存就返回 结果null */
    public boolean justReadCache() {
        return mJustReadCache;
    }

    /** 设置如果读取到缓存就忽略缓存过期时间,如果读取到了缓存就传递结果结束；如果没有读取到就会读取网络 */
    public void setReadCacheWithoutTimeLimit() {
        mReadCacheWithoutTimeLimit = true;
    }

    /** 设置只是读取缓存 */
    public void setJustReadCache() {
        mJustReadCache = true;
    }

    //------------------
    /** 是否跳过缓存，但是请求成功后会把结果保存到缓存 */
    private boolean mShouldSkipCache = false;

    /** 跳过缓存，但是请求成功后会把结果保存到缓存; 设置后会覆盖 {@link #setReadCacheWithoutTimeLimit()} */
    public void skipCache() {
        mShouldSkipCache = true;
    }

    public boolean isSkipCache() {
        return mShouldSkipCache;
    }

}
