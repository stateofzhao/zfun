package com.zfun.learn.architecture.flux;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.ArrayList;
import java.util.List;

/**
 * flux数据流向：
 * Views ---> (actions) ----> Dispatcher ---> (registered callback) ---> Stores -------+
 * Ʌ                                                                                   |
 * |                                                                                   V
 * +-- (Controller-Views "change" event handlers) <---- (Stores emit "change" events) --+
 *
 * <P/>
 * Dispatcher（Action分发器），是一个巨大的回调函数登记表。就好比一个坐在电话总机前的接线员。
 * 它保存着所有需要发送 action 的 store 列表。
 *
 * 负责把Action分发到所有注册的Store中，注意这里不是拦截器模式，它不管Store是否关注这个Action，都会给其发送过去。
 *
 * Dispatcher 的行为是同步的，这对我之前讲的多个乒乓球游戏有所帮助。
 * 如果想要在 store 之间实现依赖，有的更新完了其他的才能更新，你可以使用 Dispatcher 提供的 waitFor() 来实现。
 * Flux 的 Dispatcher 不同于其他大部分架构中的 dispatcher。它会把 action 传递给所有登记在册的 store，而不在 action 的类型。也就是说 store 并不是订阅某些 action，而是聆听每一个 action，从中过滤它关心的。
 * <P/>
 * todo zfun 两个疑惑：
 * 1，如果想要在 store 之间实现依赖，有的更新完了其他的才能更新，这个如何实现？
 * 2，两个 store 之间如何互相获取数据？
 *
 * <P/>
 * Created by Diagrams on 2016/8/12 17:03
 */
class Dispatcher {
  private static final int ACTION_DISPATCH = 2;
  private static final int STORE_REGISTER = 3;
  private static final int STORE_UNREGISTER = 4;

  private static volatile Dispatcher singleton;

  private final List<Store> storeList;
  private final Handler handler;

  private Dispatcher() {
    this.storeList = new ArrayList<>();

    //FIXME 这里可以使用其他线程的Handler
    handler = new DispatcherHandler(Looper.getMainLooper(), this);
  }

  /** 获取Dispatcher的单例 */
  public static Dispatcher get() {
    if (null == singleton) {
      synchronized (Dispatcher.class) {
        if (null == singleton) {
          singleton = new Dispatcher();
        }
      }
    }
    return singleton;
  }

  /** 开始分发Action */
  public void dispatchAction(Action action) {
    Utils.checkNotNull(action);
    action.mark("start dispatch");
    handler.sendMessage(handler.obtainMessage(ACTION_DISPATCH, action));
  }

  /** 注册Store */
  public void registerStore(Store store) {
    Utils.checkNotNull(store);
    handler.sendMessage(handler.obtainMessage(STORE_REGISTER, store));
  }

  /** 取消注册Store */
  public void unRegisterStore(Store store) {
    Utils.checkNotNull(store);
    handler.sendMessageAtFrontOfQueue(handler.obtainMessage(STORE_UNREGISTER, store));
  }

  //执行注册Store的操作
  private void performRegisterStore(Store store) {
    Utils.checkNotNull(store);
    storeList.add(store);
  }

  //执行取消注册Store的操作
  private void performUnRegisterStore(Store store) {
    Utils.checkNotNull(store);
    storeList.remove(store);
  }

  //解析Action
  private void performDispatchAction(Action action) {
    action.mark("start dispatch ");
    boolean hit = false;
    for (Store store : storeList) {
      boolean temp = store.onAction(action);
      if (!hit && temp) {
        hit = true;
      }
    }
    if (hit) {
      action.mark("end dispatch ");
    } else {
      action.mark("end dispatch : no Store to hit this action ");
    }
    action.finish();
  }

  private static class DispatcherHandler extends Handler {
    Dispatcher dispatcher;

    DispatcherHandler(Looper looper, Dispatcher dispatcher) {
      super(looper);
      this.dispatcher = dispatcher;
    }

    @Override public void handleMessage(Message msg) {
      switch (msg.what) {
        case ACTION_DISPATCH: {
          Action action = (Action) msg.obj;
          dispatcher.performDispatchAction(action);
          break;
        }
        case STORE_REGISTER: {
          Store store = (Store) msg.obj;
          dispatcher.performRegisterStore(store);
          break;
        }
        case STORE_UNREGISTER: {
          Store store = (Store) msg.obj;
          dispatcher.performUnRegisterStore(store);
          break;
        }
      }//switch end
    }
  }//class DispatcherHandler end
}
