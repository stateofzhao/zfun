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

  //解析Action
  private void performAction(Action action) {
    action.mark("start perform ");
    action = decorateAction(action);
    boolean hit = false;
    for (Store store : storeList) {
      boolean temp = store.onAction(action);
      if (!hit && temp) {
        hit = true;
      }
    }
    if (hit) {
      action.mark("end perform ");
    } else {
      action.mark("end perform : no Store to hit this action ");
    }
  }

  private Action decorateAction(Action action) {
    action.mark("start decorate ");
    for (ActionInterceptor interceptor : interceptorList) {
      action = interceptor.wrapAction(action);
    }
    action.mark("end decorate ");
    return action;
  }

}
