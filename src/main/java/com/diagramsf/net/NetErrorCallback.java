package com.diagramsf.net;

import com.android.volley.VolleyError;

/**
 * 显示网络加载错误 回调接口
 * <p/>
 * Created by Diagrams on 2016/3/15 14:06
 */
public interface NetErrorCallback {

    /**
     * @param error 网络加载错误类型
     * @param type  关注排行榜的类型
     */
    void showAppNetError(VolleyError error, int type);
}
