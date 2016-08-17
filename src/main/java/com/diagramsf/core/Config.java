package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/15 18:27
 */
public class Config {

  public static class ConfigAction {

  }

  /**
   * 框架的配置Store
   *
   * Created by Diagrams on 2016/8/15 18:26
   */
  public static class ConfigStore implements Store {

    public ConfigStore() {
    }

    @Override public boolean onAction(Action action) {
      return bindToTarget(action);
    }

    @Override public void emitStoreChange() {

    }

    private boolean bindToTarget(Action action) {
      if (action instanceof TargetAction) {
        action.mark("hit by Store: " + this);
        Target target = ((TargetAction) action).target;
        target.showStore();
        return true;
      }
      return false;
    }
  }
}
