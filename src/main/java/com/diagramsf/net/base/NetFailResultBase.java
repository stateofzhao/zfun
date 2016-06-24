package com.diagramsf.net.base;

import com.diagramsf.net.NetRequest;


/** 网络请求失败的通用结果 */
public abstract class NetFailResultBase implements NetRequest.NetFailResult {
    private Object mDeliverToResultTag;

    @Override
    public Object getRequestTag() {
        return mDeliverToResultTag;
    }

    public void setDeliverToResultTag(Object tag) {
        mDeliverToResultTag = tag;
    }
}
