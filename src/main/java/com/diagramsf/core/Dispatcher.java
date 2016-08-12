package com.diagramsf.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diagrams on 2016/8/12 17:03
 */
public class Dispatcher {
  private List<Store> storeList;

  public Dispatcher() {
    init();
  }

  public void dispatch(Action action) {
    if (hitDescribe(action)) {
      action.log.mark("hit");
    } else {
      //TODO 没有能够处理Action的Store，需要做默认处理
    }
  }

  public void regist(Store store) {
    storeList.add(store);
  }

  public void unRegist(Store store) {
    storeList.remove(store);
  }

  private void init() {
    storeList = new ArrayList<>();
  }

  private boolean hitDescribe(Action action) {
    //TODO 解析Action找到合适的Store来处理Action
    return false;
  }

  private boolean readtextDescribe() {
    return false;
  }
}
