package com.zfun.learn.architecture.flux;

/**
 * Store 保存了整个程序的状态，而且状态变化的逻辑都在 Store 里。
 *
 * 所有的状态变化都必须由它来亲自操作的。而且你不能直接通过 store 来更改状态。在 store 上并没有 setter API。
 * 要更新一次状态，你必须经过正当的手续——必须通过 Action Creator/Dispatcher 通道。
 *
 *  store 在 dispatcher 中注册了，那所有的 action 都会发给它。
 *  在 store 中，通常会使用一个 switch 语句来判断 action 的类型，决定是否对这个 action 做出相应。
 *  如果 store 关心这个 action，就会根据 action 找出需要变化的部分，更新 state。
 *  只要 store 对 state 做出了变更，就会触发 change 事件，通知视图控制器状态已经变化了。
 *
 *  它对接视图控制器-(ViewController+View)
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
