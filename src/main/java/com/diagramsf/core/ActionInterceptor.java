package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/15 18:58
 */
public interface ActionInterceptor {
  Action createAction();

  boolean intercept(Action action);
}
