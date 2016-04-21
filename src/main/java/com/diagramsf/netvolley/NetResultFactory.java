package com.diagramsf.netvolley;


import com.diagramsf.net.NetRequest;
import com.diagramsf.netvolley.volleyrequest.VolleyNetRequest;

import java.util.Map;

/**
 * 对服务器返回的结果进行解析，同时可以决策{@link VolleyNetRequest}的类型。
 *
 * @version 2.0.0
 */
public interface NetResultFactory {

    /**
     * 对服务器返回的字符串 进行操作
     * e
     *
     * @param result         服务器返回的结果
     * @param responseHeader 服务器返回结果的header
     *
     * @return 返回操作完的结果
     */
    NetRequest.NetSuccessResult analysisResult(byte[] result, Map<String, String> responseHeader) throws Exception;

    /**
     * 启用哪个 {@link VolleyNetRequest}，自定义Request可以继承{@link VolleyNetRequest}来实现
     *
     * @return 如果返回null，那么会使用{@link VolleyNetRequest}来执行网络请求
     */
    Class<? extends VolleyNetRequest> whichRequest();

}