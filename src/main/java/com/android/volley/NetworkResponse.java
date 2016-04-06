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

import org.apache.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

/**
 * 从{@link Network#performRequest(Request)} 返回的数据和 headers </p>
 * 持有的数据有:
 * 1.HTTP 响应的状态码，</p>
 * 2.响应的原始数据，byte[] </p>
 * 3.Response 的报头信息 headers</p>
 * 4.维护一个Boolean变量 标记请求结果是否更新（是否是 304 响应）。</p>
 * 
 * 
 * Data and headers returned from {@link Network#performRequest(Request)}.
 */
public class NetworkResponse {
    /**
     * Creates a new network response.
     * @param statusCode the HTTP status code
     * @param data Response body
     * @param headers Headers returned with this response, or null for none
     * @param notModified True if the server returned a 304 and the data was already in cache(304响应 客户端中所请求资源的缓存仍然是有效的,也就是说该资源从上次缓存到现在并没有被修改过)
     */
    public NetworkResponse(int statusCode, byte[] data, Map<String, String> headers,
            boolean notModified) {
        this.statusCode = statusCode;
        this.data = data;
        this.headers = headers;
        this.notModified = notModified;
    }

    public NetworkResponse(byte[] data) {
        this(HttpStatus.SC_OK, data, Collections.<String, String>emptyMap(), false);
    }

    public NetworkResponse(byte[] data, Map<String, String> headers) {
        this(HttpStatus.SC_OK, data, headers, false);
    }

    /** The HTTP status code. */
    public final int statusCode;

    /** Raw data from this response. */
    public final byte[] data;

    /** Response headers. */
    public final Map<String, String> headers;

    /** True if the server returned a 304 (Not Modified). */
    public final boolean notModified;
    
    
  //----------------------------------------
  	/** 结果是否来自缓存 */
  	private boolean isFromCache = false;

  	public void setFromCache(boolean is) {
  		this.isFromCache = is;
  	}

  	/**
  	 * 结果是否来自缓存
  	 * 
  	 * @return true 是，
  	 */
  	public boolean isFromCache() {
  		return isFromCache;
  	}
}