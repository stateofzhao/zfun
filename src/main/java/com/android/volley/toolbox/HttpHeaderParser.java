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

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.protocol.HTTP;

import java.util.Map;

/**
 * 解析HTTP headers的工具类。</p>
 *
 *
 * Utility methods for parsing HTTP headers.
 */
public class HttpHeaderParser {

  /**
   * 根据 {@link NetworkResponse} 生成{@link Cache.Entry}.</p>
   * 在这里简单的介绍下 报头信息中的几个字段的意思：
   * 1.Date 表示消息发送的时间，</p>
   * 2.Cache-Control  指定请求和响应遵循的缓存机制 值为：Public、Private、no-cache、no-store、max-age、min-fresh和max-stale，
   *
   * Extracts a {@link Cache.Entry} from a {@link NetworkResponse}.
   *
   * @param response The network response to parse headers from
   * @return a cache entry for the given response, or null if the response is not cacheable.
   */
  public static Cache.Entry parseCacheHeaders(NetworkResponse response) {
    long now = System.currentTimeMillis();

    Map<String, String> headers = response.headers;

    long serverDate = 0; //服务器返回在报头中的时间（服务器发送这个响应的服务器当前时间）,从1970年午夜 开始的毫秒数。
    long serverExpires = 0;//服务器返回的过期时间
    long softExpire = 0;//
    long maxAge = 0;
    boolean hasCacheControl = false;

    String serverEtag = null;
    String headerValue;

    headerValue = headers.get("Date");
    if (headerValue != null) {
      serverDate = parseDateAsEpoch(headerValue);
    }

    headerValue = headers.get("Cache-Control");
    if (headerValue != null) {
      hasCacheControl = true;
      String[] tokens = headerValue.split(",");
      for (int i = 0; i < tokens.length; i++) {
        String token = tokens[i].trim();
        if (token.equals("no-cache") || token.equals("no-store")) {//服务器不允许缓存此应答信息
          return null;
        } else if (token.startsWith("max-age=")) {//解析服务器指定的缓存此信息的最长时效
          try {
            maxAge = Long.parseLong(token.substring(8));
          } catch (Exception e) {
          }
          //响应在特定条件下会被重用，以满足接下来的请求，但是它必须到服务器端去验证它是不是仍然是最新的
        } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
          maxAge = 0;
        }
      }
    }

    headerValue = headers.get("Expires");
    if (headerValue != null) {
      serverExpires = parseDateAsEpoch(headerValue);
    }

    //当资源过期时（使用Cache-Control标识的max-age），发现资源具有Last-Modified（Etag）声明，
    // 则再次向web服务器请求时带上头 If-Modified-Since（If-None-Match），表示客户端请求时间。
    // web服务器收到请求后发现有头If-Modified-Since（If-None-Match）则与被请求资源的最后修改时间进行比对。
    // 若最后修改时间较新，说明资源又被改动过，则响应整片资源内容（写在响应消息包体内），HTTP 200；
    // 若最后修改时间较旧，说明资源无新修改，则响应HTTP 304 (无需包体，节省浏览)，
    // 告知浏览器继续使用所保存的cache。
    serverEtag = headers.get("ETag");

    // 如果两者同时存在，Cache-Control优先级比Expires优先级高，
    // Cache-Control takes precedence over an Expires header, even if both exist and Expires
    // is more restrictive.
    if (hasCacheControl) {
      softExpire = now + maxAge * 1000;
    } else if (serverDate > 0 && serverExpires >= serverDate) {
      // Default semantic for Expire header in HTTP specification is softExpire.
      softExpire = now + (serverExpires - serverDate);
    }

    Cache.Entry entry = new Cache.Entry();
    entry.data = response.data;
    entry.etag = serverEtag;
    entry.softTtl = softExpire;
    entry.ttl = entry.softTtl;
    entry.serverDate = serverDate;
    entry.responseHeaders = headers;

    return entry;
  }

  /**
   * 根据 RFC1123格式 解析data，从 1970 午夜开始 返回毫秒数。</p>
   *
   *
   * Parse date in RFC1123 format, and return its value as epoch
   */
  public static long parseDateAsEpoch(String dateStr) {
    try {
      // Parse date in RFC1123 format if this header contains one
      return DateUtils.parseDate(dateStr).getTime();
    } catch (DateParseException e) {//if the value could not be parsed using any of the supported date formats
      // Date in invalid format, fallback to 0
      //时间格式不对就返回0.
      return 0;
    }
  }

  /**
   * Returns the charset specified in the Content-Type of this header,
   * or the HTTP default (ISO-8859-1) if none can be found.
   */
  public static String parseCharset(Map<String, String> headers) {
    String contentType = headers.get(HTTP.CONTENT_TYPE);
    if (contentType != null) {
      String[] params = contentType.split(";");
      for (int i = 1; i < params.length; i++) {
        String[] pair = params[i].trim().split("=");
        if (pair.length == 2) {
          if (pair[0].equals("charset")) {
            return pair[1];
          }
        }
      }
    }

    return HTTP.DEFAULT_CONTENT_CHARSET;
  }
}
