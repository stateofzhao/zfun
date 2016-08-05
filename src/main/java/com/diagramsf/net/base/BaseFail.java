package com.diagramsf.net.base;

import android.util.Log;
import com.diagramsf.net.NetContract;

/** 网络请求失败的通用结果 */
public abstract class BaseFail implements NetContract.Fail {
  private Object mDeliverToResultTag;
  private Exception e;

  @Override public void setException(Exception e) {
    this.e = e;
  }

  @Override public Exception getException() {
    return e;
  }

  @Override public Object getRequestTag() {
    return mDeliverToResultTag;
  }

  @Override public void setDeliverToResultTag(Object tag) {
    mDeliverToResultTag = tag;
  }

  @Override public void logFailInfo(String tag) {
    Log.e(tag, getException().getLocalizedMessage());
  }
}
