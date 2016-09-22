package com.diagramsf.core;

/**
 * 触发操作
 *
 * Created by Diagrams on 2016/8/12 14:12
 */
public class Action {
  private FluxLog log;
  public String textDescribe;

  public Action(String textDescribe) {
    this.textDescribe = textDescribe;
    this.log = new FluxLog(this);
    mark("Action created");
  }

  public void mark(String text) {
    log.mark(text);
  }
}