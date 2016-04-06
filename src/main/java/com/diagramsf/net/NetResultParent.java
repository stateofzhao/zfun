package com.diagramsf.net;

/**
 * 实现{@link NetResult} ,对一些不必要的抽象方法进行屏蔽
 */
public abstract class NetResultParent implements NetResult {

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
    public void setRequestDeliveredTag(Object tag) {
        this.tag = tag;
    }

    @Override
    public Object getRequestDeliveredTag() {
        return tag;
    }

}
