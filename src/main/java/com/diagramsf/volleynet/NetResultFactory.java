package com.diagramsf.volleynet;


import com.diagramsf.net.NetContract;

import java.util.Map;

/**
 * 对服务器返回的结果进行解析
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
    NetContract.NetSuccessResult analysisResult(byte[] result, Map<String, String> responseHeader) throws Exception;

}