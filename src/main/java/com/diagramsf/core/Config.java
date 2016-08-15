package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/15 18:27
 */
public class Config {

  public static class ConfigAction{

  }

  /**
   * 框架的配置Store
   *
   * Created by Diagrams on 2016/8/15 18:26
   */
  public static class ConfigStore implements Store {
    @Override public boolean onAction(Action action) {
      return false;
    }
  }
}
