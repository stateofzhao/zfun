package com.diagramsf.executor;

/**
 * {@link Task}基类，缩小了setter和getter方法。<P>
 *
 * Created by Diagrams on 2016/8/9 11:50
 */
public abstract class BaseTask implements Task {
  private @Priority int priority;

  @Override final public int getPriority() {
    return priority;
  }

  @Override final public void priority(@Priority int priority) {
    this.priority = priority;
  }

  @Override public void onStateChange(@State int state) {

  }
}
