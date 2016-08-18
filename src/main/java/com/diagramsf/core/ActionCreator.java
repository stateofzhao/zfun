package com.diagramsf.core;

/**
 * //http://www.jianshu.com/p/896ce1a8e4ed
 *
 * Created by Diagrams on 2016/8/12 17:00
 */
public class ActionCreator {
  private Dispatcher dispatcher;

  public ActionCreator() {
    dispatcher = new Dispatcher();
  }

  public void sendAction(String textDescribe) {
    Action action = new Action(textDescribe);
    dispatcher.dispatchAction(action);
  }
}
