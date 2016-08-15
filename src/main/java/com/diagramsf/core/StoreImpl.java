package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/12 17:03
 */
public class StoreImpl implements Store {
  private Target target;

  public StoreImpl(Target target) {
    this.target = target;
  }

  @Override
  public boolean onAction(Action action) {
    return false;
  }

  private void bindToTarget(Action action) {
    target.showStore(this);
  }
}
