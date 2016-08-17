package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/17 10:07
 */
public class TargetAction extends Action {
  public Target target;

  public TargetAction(String textDescribe, Target target) {
    super(textDescribe);
    this.target = target;
  }
}
