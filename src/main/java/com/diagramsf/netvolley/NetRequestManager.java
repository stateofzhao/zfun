/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.diagramsf.net.NetContract;

import java.io.File;
import java.util.Map;

/**
 * 调用{@link NetRequestManager#with(Context)}来获取Volley，调用{@link NetRequestManager#load(Request)}来执行请求
 * <p>
 * 如果只是默认配置，直接调用{@link NetRequestManager#with(Context)}来实例化即可；
 * 如果需要自定义参数配置，那么需要自己调用{@link Builder#build()}来构建Volley，之后保存构建的Volley。
 * <p>
 * 仿照Picasso的API方式（采用Build模式）来实现
 */
public class NetRequestManager {
    private static volatile NetRequestManager singleton;

    private RequestQueue requestQueue;

    private NetRequestManager(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public static NetRequestManager with(Context context) {
        if (singleton == null) {
            synchronized (NetRequestManager.class) {
                if (singleton == null) {
                    singleton = new Builder(context).build();
                }
            }
        }
        return singleton;
    }

    /** 取消请求 */
    public void cancel(Object tag) {
        requestQueue.cancelAll(tag);
    }

    /**
     * @param url    请求的网址
     * @param method {@link com.android.volley.Request.Method}中的一种
     */
    public RequestCreator load(String url, int method) {
        return new RequestCreator(this, url, method);
    }

    /**
     * @param url 请求的网址
     */
    public RequestCreator load(String url) {
        return new RequestCreator(this, url, Request.Method.DEPRECATED_GET_OR_POST);
    }

    public void load(Request request) {
        requestQueue.add(request);
    }

    public static class Builder {
        /** Default on-disk cache directory. */
        private final String DEFAULT_CACHE_DIR = "volleyRequestManager";
        /** 磁盘缓存最大大小 */
        public final int DEFAULT_SIZE_EXTERNAL_CACHE = 500 * 1024 * 1024;

        private final Context context;
        /** 用户代理，会写入到Http报头中 */
        private String userAgent;
        private Cache cache;
        private HttpStack httpStack;

        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null");
            }
            this.context = context;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder cache(Cache cache) {
            this.cache = cache;
            return this;
        }

        public Builder httpStack(HttpStack httpStack) {
            this.httpStack = httpStack;
            return this;
        }

        public NetRequestManager build() {
            if (null == userAgent || "".equals(userAgent)) {
                this.userAgent = Utils.createUserAgent(context);
            }

            if (null == cache) {
                File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
                cache = new DiskBasedCache(cacheDir,
                        DEFAULT_SIZE_EXTERNAL_CACHE);
            }

            if (null == httpStack) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {//2.3 API 9
                    httpStack = new HurlStack();// 使用HttpURLConnection实现的HttpStack
                } else {
                    // Prior to Gingerbread, HttpUrlConnection was unreliable.
                    // See:
                    // http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                    httpStack = new HttpClientStack(
                            AndroidHttpClient.newInstance(userAgent)); // 使用HttpClient客户端
                }
            }

            Network network = new BasicNetwork(httpStack);
            RequestQueue queue = new RequestQueue(cache, network);
            queue.start();

            return new NetRequestManager(queue);
        }
    }// class Builder end

    public static class RequestCreator {
        private String url;
        private int method = -1;
        private Map<String, String> postData;
        private Map<String, String> header;
        private RetryPolicy retryPolicy;
        private Request.Priority priority;
        private Object cancelTag;
        private Object deToResultTag;
        private NetContract.NetResultErrorListener errorListener;
        private NetContract.NetResultListener listener;
        private int type;
        private String cacheKey;

        private NetRequestManager volleyRequestManager;

        /** @param method 是{@link Request.Method}中的一种 */
        private RequestCreator(NetRequestManager volleyRequestManager, String url, int method) {
            this.volleyRequestManager = volleyRequestManager;
            this.url = url;
            this.method = method;
        }

        /** post传递的参数 */
        public RequestCreator postData(Map<String, String> postData) {
            this.postData = postData;
            return this;
        }

        /** 加载失败回调接口 */
        public RequestCreator errorListener(NetContract.NetResultErrorListener listener) {
            errorListener = listener;
            return this;
        }

        /** 加载成功回调接口 */
        public RequestCreator listener(NetContract.NetResultListener listener) {
            this.listener = listener;
            return this;
        }

        /** 加载失败重试策略 */
        public RequestCreator retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        /** 优先级 */
        public RequestCreator Priority(Request.Priority priority) {
            this.priority = priority;
            return this;
        }

        /** 自定义包头信息 */
        public RequestCreator header(Map<String, String> header) {
            this.header = header;
            return this;
        }

        /** 请求的tag，可以用来取消请求 */
        public RequestCreator tag(Object cancelTag) {
            this.cancelTag = cancelTag;
            return this;
        }

        /** 请求类型，参见 {@link com.diagramsf.net.NetContract.Type} */
        public RequestCreator type(@NetContract.Type int type) {
            this.type = type;
            return this;
        }

        public RequestCreator deliverToResultTag(Object object) {
            deToResultTag = object;
            return this;
        }

        public RequestCreator cacheKey(String cacheKey) {
            this.cacheKey = cacheKey;
            return this;
        }

        /** 使用结果解析器开始请求网络 */
        public void into(NetResultFactory factory) {
            volleyRequestManager.load(createRequest(factory));
        }

        private Request createRequest(NetResultFactory factory) {
            if (null == url || "".equals(url)) {
                throw new IllegalArgumentException("URL must not be empty!");
            }

            if (-1 == method) {
                method = Request.Method.DEPRECATED_GET_OR_POST;
            }

            VolleyNetRequest request = new VolleyNetRequest(method, url, postData,
                    header, priority, factory, null);
            if (null != retryPolicy) {
                request.setRetryPolicy(retryPolicy);
            }
            request.setTag(cancelTag);
            request.setErrorListener(errorListener);
            request.setListener(listener);
            request.request(type);
            request.setCacheKey(cacheKey);
            request.setDeliverToResultTag(deToResultTag);
            return request;
        }

    }// class RequestCreator end

    public static class Utils {
        public static String createUserAgent(Context context) {
            // 定义用户代理
            String userAgent = "volleyRequestManager/0";
            try {
                String packageName = context.getPackageName();
                PackageInfo info = context.getPackageManager().getPackageInfo(
                        packageName, 0);
                //            String appName = info.applicationInfo.loadLabel(
                //                    context.getPackageManager()).toString();
                String deviceName = Build.MANUFACTURER;
                String os_version = Build.VERSION.RELEASE;//系统版本
                //            String app_version = info.versionName;
                String able = context.getResources().getConfiguration().locale
                        .getCountry();
                //将appName修改为packageName，防止出现中文无法解析
                userAgent = packageName + " (" + deviceName
                        + "; android " + os_version + "; " + able + ")";
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            return userAgent;
        }
    }// class Utils end

}// class NetRequestManager end
