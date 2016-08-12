package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/12 17:03
 */
public class Store {
  private Target target;

  public Store(Target target) {
    this.target = target;
  }

  public boolean analysisAction(Action action) {
    return false;
  }

  public void bindToTarget(Action action) {
    target.showAction(action);
  }
}
