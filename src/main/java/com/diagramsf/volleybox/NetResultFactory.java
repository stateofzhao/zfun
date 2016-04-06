package com.diagramsf.volleybox;


import com.diagramsf.net.NetResult;

import java.util.Map;

/**
 * 对服务器返回的结果进行解析，同时可以决策{@link NetResultRequest}的类型。
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
    NetResult analysisResult(byte[] result, Map<String, String> responseHeader) throws Exception;

    /**
     * 启用哪个 {@link NetResultRequest}，自定义Request可以继承{@link NetResultRequest}来实现
     *
     * @return 如果返回null，那么会使用{@link NetResultRequest}来执行网络请求
     */
    Class<? extends NetResultRequest> whichRequest();

}