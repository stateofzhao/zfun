package com.diagramsf.core;

/**
 * Created by Diagrams on 2016/8/15 17:18
 */
public abstract class Store {
  private OnStoreChangeListener listener;

  interface OnStoreChangeListener {
    void onChange(Store store);
  }

  public abstract boolean onAction(Action action);

  public void setOnChangeListener(OnStoreChangeListener listener) {
    Utils.checkNotNull(listener);
    this.listener = listener;
  }

  void emitStoreChange() {
    listener.onChange(this);
  }
}
