package com.diagramsf.core;

/**
 * 维护一组UI或者一个页面UI的状态（持有所有UI需要的数据，主要作用就是架空UI，
 * 是UI只是用来展示数据用的不涉及任何逻辑操作）
 *
 * Created by Diagrams on 2016/8/15 17:18
 */
public abstract class Store {
  private OnStoreChangeListener listener;

  interface OnStoreChangeListener {
    void onStoreChanged();
  }

  abstract boolean onAction(Action action);

  public void setOnChangeListener(OnStoreChangeListener listener) {
    Utils.checkNotNull(listener);
    this.listener = listener;
  }

  void emitStoreChange() {
    listener.onStoreChanged();
  }
}
