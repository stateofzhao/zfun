package com.diagramsf.netrequest.nocacherequest;

import com.diagramsf.netrequest.OnNetRequestFinishListener;
import com.diagramsf.volleybox.NetResultFactory;

/**
 * 执行请求数据的 交互器 MVP中的M,负责处理数据的输入和输出（数据的来源）
 * <p/>
 * Created by Diagrams on 2015/8/13 15:16
 */
public interface NoCacheRequestInteractor {

    /**
     * 这个方法必须在{@link #request(String, String, String, NetResultFactory, OnNetRequestFinishListener)}之前
     * 调用，否则不起作用，如果要改变此值就必须在调用 {@link #request(String, String, String, NetResultFactory, OnNetRequestFinishListener)}
     * 之前更改
     */
    void setDeliverToResultTag(Object tag);

    /** 请求网络数据 */
    void request(String url, String postData, String cancelTag, NetResultFactory factory,
                 OnNetRequestFinishListener listener);

    /**
     * 取消网络请求
     *
     * @param cancelTag {@link #request(String, String, String, NetResultFactory, OnNetRequestFinishListener)}
     *                  中的cancelTag
     */
    void cancelRequest(String cancelTag);
}
