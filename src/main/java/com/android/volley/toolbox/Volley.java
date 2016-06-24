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

package com.android.volley.toolbox;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

import java.io.File;

/**
 * 调用{@link Volley#with(Context)}来获取Volley，调用{@link Volley#add(Request)}来执行请求
 * <p>
 * 如果只是默认配置，直接调用{@link Volley#with(Context)}来实例化即可；
 * 如果需要自定义参数配置，那么需要自己调用{@link Builder#build()}来构建Volley，之后保存构建的Volley。
 * <p>
 * ===仿照Picasso的API方式（采用Build模式）来实现
 */
public class Volley {
    private static volatile Volley singleton;

    private RequestQueue requestQueue;

    private Volley(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public static Volley with(Context context) {
        if (singleton == null) {
            synchronized (Volley.class) {
                if (singleton == null) {
                    singleton = new Builder(context).build();
                }
            }
        }
        return singleton;
    }

    public <T> Request<T> add(Request<T> request) {
        return requestQueue.add(request);
    }

    public static class Builder {
        /** Default on-disk cache directory. */
        private final String DEFAULT_CACHE_DIR = "volley";
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

        public Volley build() {
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

            return new Volley(queue);
        }
    }// class Builder end

    public static class Utils {

        public static String createUserAgent(Context context) {
            // 定义用户代理
            String userAgent = "volley/0";
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

}// class Volley end
