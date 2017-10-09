package com.diagramsf.executor;

import android.os.Handler;
import android.os.Looper;

/**
 * {@link Task}基类，缩小了setter和getter方法。<P>
 *
 * Created by Diagrams on 2016/8/9 11:50
 */
public abstract class BaseTask implements Task {
  protected static Handler mMainHandler = new Handler(Looper.getMainLooper());

  private @State int state;
  private @Priority int priority;

  @Override final public int getPriority() {
    return priority;
  }

  @Override final public void priority(@Priority int priority) {
    this.priority = priority;
  }

  @Override public void onStateChange(@State int state) {
    this.state = state;
  }

  protected boolean isCancel(){
    return state == Task.CANCEL;
  }
}
