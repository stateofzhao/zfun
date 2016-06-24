package com.diagramsf.netvolley;

import com.diagramsf.helpers.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Diagrams on 2016/6/24 13:45
 */
public class Utils {
    public static final String PROTOCOL_CHARSET = "utf-8";

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
}
