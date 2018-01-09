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

import com.android.volley.toolbox.HttpStack;
import java.util.Map;

/**
 * 一个处理 requests 的接口.<br/>
 * **注意：**与{@link HttpStack}的区别，这个是负责本框架中整个与网络有关的所有处理（包括：
 * 1.)解析缓存中的报头信息，然后把此报头（Map<String,String>）作为额外报头作为第二个参数传递给{@link HttpStack#performRequest(Request,
 * Map)}方法，{@link HttpStack#performRequest(Request, Map)}需要结合{@link Request#getHeaders()}来一块处理。；
 * 2.)
 * <P>
 * An interface for performing requests.
 */
public interface Network {
  /**
   * 处理指定的 request
   * <p>
   * Performs the specified request.
   *
   * @param request Request to process
   * @return A {@link NetworkResponse} with data and caching metadata; will never be null
   * @throws VolleyError on errors
   */
  public NetworkResponse performRequest(Request<?> request) throws VolleyError;
}
