package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/15 18:58
 */
public interface ActionInterceptor {
  /** 对{@link Action}进行包装 */
  Action wrapAction(Action original);
}
