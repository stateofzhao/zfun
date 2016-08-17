package com.diagramsf.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diagrams on 2016/8/12 17:03
 */
public class Dispatcher {
  private List<Store> storeList;

  public Dispatcher() {
    storeList = new ArrayList<>();
  }

  public void dispatch(Action action) {
    action.mark("dispatch");
    boolean hit = analysisAction(action);
    for (Store store : storeList) {
      boolean temp = store.onAction(action);
      if (!hit && temp) {
        hit = true;
      }
    }

    if (!hit) {
      action.mark("no Store to hit this action,action finish");
    }
  }

  public void register(Store store) {
    Utils.checkNotNull(store);
    storeList.add(store);
  }

  public void unRegister(Store store) {
    Utils.checkNotNull(store);
    storeList.remove(store);
  }

  //分析Action
  private boolean analysisAction(Action action) {
    return false;
  }
}
