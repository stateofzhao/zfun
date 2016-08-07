package com.diagramsf.net.base;

import com.diagramsf.net.NetContract;

/**
 * 实现{@link NetContract.Result} ,对一些不必要的抽象方法进行屏蔽
 */
public class BaseResult implements NetContract.Result {
  private ResultType resultType;
  private Object tag;

  @Override public void setResultType(ResultType resultType) {
    this.resultType = resultType;
  }

  @Override public ResultType getResultType() {
    return resultType;
  }

  @Override public boolean checkResultLegitimacy() {
    return true;
  }

  @Override public void setRequestTag(Object tag) {
    this.tag = tag;
  }

  @Override public Object getRequestTag() {
    return tag;
  }
}
