package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/18 16:58
 */
public class FluxAndroid {
  static volatile FluxAndroid singleton = null;

  ActionCreator actionCreator;

  FluxAndroid(ActionCreator actionCreator){
    this.actionCreator = actionCreator;
  }

  public static FluxAndroid instance(){
    if (singleton == null) {
      synchronized (FluxAndroid.class) {
        if (singleton == null) {
          ActionCreator creator = new ActionCreator();
          singleton = new Builder(creator).build();
        }
      }
    }
    return singleton;
  }

  static class Builder{
    ActionCreator creator;

    Builder(ActionCreator creator){
      this.creator = creator;
    }
    FluxAndroid build(){
      return new FluxAndroid(creator);
    }
  }//class Builder end
}
