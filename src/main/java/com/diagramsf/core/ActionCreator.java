package com.diagramsf.core;

/**
 * //http://www.jianshu.com/p/896ce1a8e4ed
 *
 * Created by Diagrams on 2016/8/12 17:00
 */
public class ActionCreator {
  private static volatile ActionCreator singleton = null;

  private Dispatcher dispatcher;

  private ActionCreator(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  public static ActionCreator get(Dispatcher dispatcher) {
    if (null == singleton) {
      synchronized (ActionCreator.class) {
        if (null == singleton) {
          singleton = new ActionCreator(dispatcher);
        }
      }
    }
    return singleton;
  }

  public void sendAction(String textDescribe) {
    Action action = new Action(textDescribe);
    dispatcher.dispatchAction(action);
  }
}
