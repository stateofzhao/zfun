package com.diagramsf.net.comm;

import android.content.Context;
import android.support.annotation.NonNull;
import com.android.volley.VolleyError;
import com.diagramsf.exceptions.AppException;
import com.diagramsf.helpers.AppDebugLog;
import com.diagramsf.net.NetRequest;
import com.diagramsf.netvolley.VolleyErrorHelper;


/** 网络请求失败的通用结果 */
public class NetFailResultComm implements NetRequest.NetFailResult {

    private AppException mException;
    private VolleyError mVolleyError;

    private Object mDeliverToResultTag;

    public NetFailResultComm(@NonNull AppException exception) {
        mException = exception;
    }

    public NetFailResultComm(@NonNull VolleyError exception) {
        mVolleyError = exception;
        mException = VolleyErrorHelper.formatVolleyError(exception);
    }

    public NetFailResultComm(@NonNull AppException exception, VolleyError error) {
        mException = exception;
        mVolleyError = error;
    }

    @Override
    public void setException(@NonNull AppException e) {
        this.mException = e;
    }

    @Override
    public void toastFailStr(Context context) {
        mException.makeToast(context);

    }

    @Override
    public void logFailInfo(String tag) {
        AppDebugLog.logError(tag, mException.toString());
    }

    @Override
    public String getInfoText(Context context) {
        return mException.convertExceptionToText(context);
    }

    @Override
    public Object getRequestDeliveredTag() {
        return mDeliverToResultTag;
    }

    public void setVolleyError(VolleyError error) {
        mVolleyError = error;
    }

    public VolleyError getVolleyError() {
        return mVolleyError;
    }

    public void setDeliverToResultTag(Object tag) {
        mDeliverToResultTag = tag;
    }

}
