package com.diagramsf.net;

import android.content.Context;
import com.diagramsf.exceptions.AppException;

/** 网络请求失败的结果 */
public interface NetFailedResult {

    /** 设置结果的异常 */
    void setException(AppException e);

    /** 提示异常信息,这个是给用户看的 */
    void toastFailStr(Context context);

    /** 打印异常信息，这个是调试程序用的 */
    void logFailInfo(String tag);

    /** 获取异常信息的 文字描述 */
    String getInfoText(Context context);

    /** 获取通过 {@link NetRequest#setDeliverToResultTag(Object)} 设置的值 */
    Object getRequestDeliveredTag();
}
