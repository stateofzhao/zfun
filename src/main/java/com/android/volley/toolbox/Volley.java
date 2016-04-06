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
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.diagramsf.helpers.FileUtils;
import com.diagramsf.helpers.OSVersionUtils;
import com.diagramsf.volleybox.OkHttpStack;

import java.io.File;

/**
 * 负责获取请求队列{@link RequestQueue}
 */
public class Volley {

    /** Default on-disk cache directory. */
    // private static final String DEFAULT_CACHE_DIR = "volley";

    public static String userAgent;

    /**
     * Creates a default instance of the worker pool and calls
     * {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @param stack   An {@link HttpStack} to use for the network, or null for
     *                default.
     *
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
        File cacheDir = new File(context.getCacheDir(), "volley");
        // 使用的内部缓存，可以根据需要自己修改缓存目录
        //		File cacheDir = FileUtils.getAppExternalCacheDir(context,
        //				FileUtils.DATA_FILE_SAVE_HEAD_PATH);
        //		if (!cacheDir.exists()) {
        //			cacheDir.mkdirs();
        //		}

        // 定义用户代理
        userAgent = "volley/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    packageName, 0);
            //            String appName = info.applicationInfo.loadLabel(
            //                    context.getPackageManager()).toString();
            String deviceName = Build.MANUFACTURER;
            String os_version = OSVersionUtils.getOs_Version();
            //            String app_version = info.versionName;
            String able = context.getResources().getConfiguration().locale
                    .getCountry();
            //将appName修改为packageName，防止出现中文无法解析
            userAgent = packageName + " (" + deviceName
                    + "; android " + os_version + "; " + able + ")";

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        // 定义默认HTTP client
        if (stack == null) {
            if (OSVersionUtils.hasGingerbread()) {
                stack = new OkHttpStack();// 使用自定义的网络客户端
            } else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See:
                // http: //
                // android-developers.blogspot.com/2011/09/androids-http-clients.html
                stack = new HttpClientStack(
                        AndroidHttpClient.newInstance(userAgent)); // 使用HttpClient
                // 客户端
            }
        }

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir,
                FileUtils.SIZE_EXTERNAL_CACHE), network);
        queue.start();

        return queue;
    }

    /**
     * Creates a default instance of the worker pool and calls
     * {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     *
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, null);
    }

}
