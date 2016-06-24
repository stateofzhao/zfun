package com.diagramsf.net.base;

import com.diagramsf.net.NetRequest;

/**
 * 实现{@link NetRequest.NetSuccessResult} ,对一些不必要的抽象方法进行屏蔽
 */
public abstract class NetSuccessResultBase implements NetRequest.NetSuccessResult {

    ResultType resultType;
    private Object tag;

    @Override
    public void setResultType(ResultType resultType) {
        this.resultType = resultType;
    }

    @Override
    public ResultType getResultType() {
        return resultType;
    }

    @Override
    public boolean checkResultLegitimacy() {
        return true;
    }

    @Override
    public void setRequestTag(Object tag) {
        this.tag = tag;
    }

    @Override
    public Object getRequestTag() {
        return tag;
    }

}
