package com.diagramsf.core;

/**
 *
 * //http://www.jianshu.com/p/896ce1a8e4ed
 *
 * Created by Diagrams on 2016/8/12 17:00
 */
public class ActionCreator {

  public Action createAction(String textDescribe) {
    return new Action(textDescribe);
  }
}
