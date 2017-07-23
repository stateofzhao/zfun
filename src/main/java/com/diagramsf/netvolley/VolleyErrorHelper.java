package com.diagramsf.netvolley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.diagramsf.net.ExceptionWrapper;
import java.util.Locale;

/** 转换 VolleyError 成错误信息 */
public class VolleyErrorHelper {

  /**
   * @return 不为空
   */
  public static String formatVolleyError(VolleyError volleyError) {

    String text;

    if (volleyError instanceof AuthFailureError) {//请求身份验证失败
      text = "服务器验证身份失败";
    } else if (volleyError instanceof NoConnectionError) {//无网络连接 或者 有可能URL拼写错误
      text = "网络连接失败，请检查网络设置";
    } else if (volleyError instanceof TimeoutError) {//请求超时
      text = "网络异常，请求超时";
    } else if (volleyError instanceof NetworkError) { // 执行网络请求时发生错误
      text = "网络异常，请求超时";
    } else if (volleyError instanceof ParseError) {//解析结果发生错误
      text = "数据解析异常";
    } else if (volleyError instanceof ServerError) { // 服务器应答，但是是错误应答
      if (null != volleyError.networkResponse) {
        text = String.format(Locale.getDefault(), "服务器错误，响应码：%d",
            volleyError.networkResponse.statusCode);
      } else {
        text = "网络异常，请求超时";
      }
    } else {
      text = "应用程序运行时异常";
    }

    return text;
  }
}//class end
