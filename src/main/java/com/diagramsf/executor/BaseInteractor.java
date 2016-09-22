package com.diagramsf.executor;

/**
 * Created by Diagrams on 2016/8/9 11:50
 */
public abstract class BaseInteractor implements Interactor {
  private boolean cancel = false;
  private @Priority int priority;

  @Override final public void cancel() {
    cancel = true;
  }

  @Override final public boolean isCancel() {
    return cancel;
  }

  @Override final public int getPriority() {
    return priority;
  }

  @Override final public void priority(@Priority int priority) {
    this.priority = priority;
  }

  @Override public void onStateChange(@State int state) {
    
  }
}
