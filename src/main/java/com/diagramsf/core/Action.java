package com.diagramsf.core;

/**
 * 触发操作
 *
 * Created by Diagrams on 2016/8/12 14:12
 */
class Action {
  private FluxLog log;
  String textDescribe;

  Action(String textDescribe) {
    this.textDescribe = textDescribe;
    this.log = new FluxLog(this);
    mark("===================");
    mark("Action created");
  }

  void mark(String text) {
    log.mark(text);
    log.mark("\n");
  }

  void finish(){
    mark("===================");
  }
}
