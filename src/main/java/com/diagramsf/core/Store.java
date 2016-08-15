package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/15 17:18
 */
public interface Store {
  /**
   * 接收到了{@link Action}
   *
   * @return 接收并且消耗Action的话返回true，否则返回false
   */
  boolean onAction(Action action);
}
