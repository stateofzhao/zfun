package com.diagramsf.volleybox;

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.Volley;
import com.diagramsf.exceptions.AppException;
import com.diagramsf.helpers.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 用来对Volley进行初始化操作，以及封装了添加请求，取消请求等基本方法。
 */
public class VolleyUtils {

    public static final String PROTOCOL_CHARSET = "utf-8";

    private Context mApplicationContext;
    private RequestQueue mRequestQueue;

    private static VolleyUtils mUtils;

    private VolleyUtils() {
    }

    public static VolleyUtils getInstance() {
        if (null == mUtils) {
            mUtils = new VolleyUtils();
        }
        return mUtils;
    }

    /** 将 Map 转换成字符串 ，会使用{@link URLEncoder}来编码Map中的key和value */
    public static String changeMapParamToStr(Map<String, String> params,
                                             String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {

            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(),
                        paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(),
                        paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: "
                    + paramsEncoding, uee);
        }
    }


    /** 将PostData 转换成Map */
    public static Map<String, String> changePostDataToMap(String postData) {
        if (StringUtils.isEmpty(postData)) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        String[] params = postData.split("\\&");
        for (String str : params) {
            // 这里要明白一点： ss="" ,这个形式返回的oneParam的length也是1
            String[] oneParam = str.split("\\=");
            result.put(oneParam[0], oneParam[1]);
        }
        return result;
    }

    /**
     * 去掉PostData中的版本控制号
     *
     * @param orangePostData 原始postData
     * @param versionName    中的版本对应的名称
     *
     * @return 去除版本号后的postData
     */
    public static String deletVersionParam(String orangePostData, String versionName) {
        Map<String, String> postMap = changePostDataToMap(orangePostData);
        if (null == postMap) {
            return null;
        }
        postMap.remove(versionName);
        return changeMapParamToStr(postMap, PROTOCOL_CHARSET);
    }


    /**
     * 检测Volley是否被初始化了
     *
     * @return true 被初始化了；false没有初始化;
     */
    private boolean checkInit() {
        if (null != mApplicationContext && null != mRequestQueue) {
            return true;
        }

        if (null != mApplicationContext) {
            mRequestQueue = Volley.newRequestQueue(mApplicationContext);
            return true;
        }

        return false;
    }

    /** 取消请求 */
    public void cancelRequest(Object tag) {
        mRequestQueue.cancelAll(tag);
    }

    /**
     * 添加请求
     *
     * @throws AppException
     */
    public void addRequest(NetResultRequest request) throws AppException {
        if (!checkInit()) {
            throw AppException.run(new Exception("VolleyUtils必须先初始化"));
        }
        mRequestQueue.add(request);
    }

    /** 初始化Volley框架 */
    public void initVolley(Context context) {
        if (null == mRequestQueue) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
        mApplicationContext = context.getApplicationContext();
    }

    /** 初始化Volley框架 */
    public void initVolley(Context context,HttpStack stack) {
        if (null == mRequestQueue) {
            mRequestQueue = Volley.newRequestQueue(context,stack);
        }
        mApplicationContext = context.getApplicationContext();
    }

    /** 关闭Volley，一般不要调用 */
    public void closeVolley() {
        if (null != mRequestQueue) {
            mRequestQueue.stop();
            mRequestQueue = null;
        }
    }
}
