package com.diagramsf.executor;

/**
 * Created by Diagrams on 2016/8/9 11:50
 */
public abstract class BaseInteractor implements Interactor {
  private boolean cancel = false;
  private @Priority int priority;

  @Override public void cancel() {
    cancel = true;
  }

  @Override public boolean isCancel() {
    return cancel;
  }

  @Override public int getPriority() {
    return priority;
  }

  @Override public void priority(@Priority int priority) {
    this.priority = priority;
  }

  @Override public void StateChange(@State int state) {
    
  }
}
