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

package com.android.volley.toolbox;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * HTTP 客户端接口， 只有一个方法 {@link HttpStack#performRequest(Request, Map)}} 具体处理Http请求
 * <P>
 * 
 * An HTTP stack abstraction.
 */
public interface HttpStack {
	/**
	 * 根据给定的属性来执行HTTP请求
	 * <p>
	 * 如果request.getPostBody()==null 执行GET request。
	 * 否则执行POST request，并把 Content-Type header 设置成 request.getPostBodyContentType()。
	 * 
	 * 
	 * <p>
	 * Performs an HTTP request with the given parameters.
	 * 
	 * <p>
	 * A GET request is sent if request.getPostBody() == null. A POST request is
	 * sent otherwise, and the Content-Type header is set to
	 * request.getPostBodyContentType().
	 * </p>
	 * 
	 * @param request
	 *            the request to perform
	 * @param additionalHeaders
	 *            additional headers to be sent together with
	 *            {@link Request#getHeaders()}
	 * @return the HTTP response
	 */
	public HttpResponse performRequest(Request<?> request,
									   Map<String, String> additionalHeaders) throws IOException,
			AuthFailureError;

}
