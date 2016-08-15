package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/12 14:12
 */
public class Action {
  public FluxLog log;
  private String textDescribe;

  public Action(String textDescribe) {
    this.textDescribe = textDescribe;
    this.log = new FluxLog(this);

    log.mark("Action created");
  }
}
