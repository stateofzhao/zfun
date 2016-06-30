package com.diagramsf.netvolley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.diagramsf.net.ExceptionWrapper;

/** 转换 VolleyError 成本地 ExceptionWrapper */
public class VolleyErrorHelper {

	public static ExceptionWrapper formatVolleyError(VolleyError volleyError) {
		if(volleyError instanceof AuthFailureError){//请求身份验证失败
			return ExceptionWrapper.auth(volleyError);
		}else if(volleyError instanceof NoConnectionError){//无网络连接 或者 有可能URL拼写错误
			return ExceptionWrapper.noNet(volleyError);
		}else if(volleyError instanceof TimeoutError){//请求超时
			return ExceptionWrapper.http(volleyError);
		}else if(volleyError instanceof NetworkError){ // 执行网络请求时发生错误
			return ExceptionWrapper.http(volleyError);
		}else if(volleyError instanceof ParseError){//解析结果发生错误
			return ExceptionWrapper.json(volleyError);
		}else if(volleyError instanceof ServerError){ // 服务器应答，但是是错误应答
			if(null != volleyError.networkResponse){
				return ExceptionWrapper.server(volleyError.networkResponse.statusCode);
			}else{
				return ExceptionWrapper.http(volleyError);
			}
		}else{
			return ExceptionWrapper.run(volleyError);
		}
	}

}//class end
