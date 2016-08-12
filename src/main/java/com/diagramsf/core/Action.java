package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/12 14:12
 */
public class Action {
  public LogPendding log;
  private String textDescribe;

  public Action(String textDescribe) {
    this.textDescribe = textDescribe;
    init();
  }

  private void init() {
    log = new LogPendding(this);
  }
}
