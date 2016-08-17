package com.diagramsf.core;

import java.util.ArrayList;
import java.util.List;

/**
 * //http://www.jianshu.com/p/896ce1a8e4ed
 *
 * Created by Diagrams on 2016/8/12 17:00
 */
public class ActionCreator {
  private List<ActionInterceptor> interceptorList;
  private Dispatcher dispatcher;

  public ActionCreator() {
    interceptorList = new ArrayList<>();
    dispatcher = new Dispatcher();
  }

  public void addInterceptor(ActionInterceptor interceptor) {
    Utils.checkNotNull(interceptor);
    interceptorList.add(interceptor);
  }

  public void removeInterceptor(ActionInterceptor interceptor) {
    Utils.checkNotNull(interceptor);
    interceptorList.remove(interceptor);
  }

  public void createAction(String textDescribe) {
    Action action = new Action(textDescribe);
    dispatcher.dispatch(decorateAction(action));
  }

  private Action decorateAction(Action action) {
    for (ActionInterceptor interceptor : interceptorList) {
      action = interceptor.wrapAction(action);
    }
    return action;
  }
}
