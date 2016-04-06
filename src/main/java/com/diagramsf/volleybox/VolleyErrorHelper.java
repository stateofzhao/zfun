package com.diagramsf.volleybox;

import android.content.Context;
import com.android.volley.*;
import com.diagramsf.exceptions.AppException;

/** 转换VolleyError 成 本地 AppException */
public class VolleyErrorHelper {

	public static AppException formatVolleyError(VolleyError volleyError) {
		if(volleyError instanceof AuthFailureError){//请求身份验证失败
			return AppException.auth(volleyError);
		}else if(volleyError instanceof NoConnectionError){//无网络连接 或者 有可能URL拼写错误
			return AppException.noNet(volleyError);
		}else if(volleyError instanceof TimeoutError){//请求超时
			return AppException.http(volleyError);
		}else if(volleyError instanceof NetworkError){ // 执行网络请求时发生错误
			return AppException.http(volleyError);
		}else if(volleyError instanceof ParseError){//解析结果发生错误
			return AppException.json(volleyError);
		}else if(volleyError instanceof ServerError){ // 服务器应答，但是是错误应答
			if(null != volleyError.networkResponse){
				return AppException.http(volleyError.networkResponse.statusCode);
			}else{
				return AppException.server(volleyError);
			}
		}else{
			return AppException.run(volleyError);
		}
	}

	/** 将Volley的失败结果信息，转换成app的友好提示展示给用户 */
	public static void showVolleyFailedMessage(Context context, VolleyError error) {
		VolleyErrorHelper.formatVolleyError(error).makeToast(context);
	}
}
