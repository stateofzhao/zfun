package com.diagramsf.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Diagrams on 2016/8/12 17:03
 */
public class Dispatcher {
  static final int ACTION_INTERCEPTOR_ADD = 1;
  static final int ACTION_DISPATCH = 2;
  static final int STORE_REGISTER = 3;
  static final int STORE_UNREGISTER = 4;

  final List<ActionInterceptor> interceptorList;
  final List<Store> storeList;
  final Handler handler;

  Dispatcher() {
    this.storeList = new ArrayList<>();
    this.interceptorList = new ArrayList<>();

    //FIXME 这里可以使用其他线程的Handler
    handler = new DispatcherHandler(Looper.myLooper(), this);
  }

  void dispatchAction(Action action) {
    Utils.checkNotNull(action);
    action.mark("start dispatch");
    handler.sendMessage(handler.obtainMessage(ACTION_DISPATCH, action));
  }

  void dispatchAddActionInterceptor(ActionInterceptor interceptor) {
    Utils.checkNotNull(interceptor);
    handler.sendMessage(handler.obtainMessage(ACTION_INTERCEPTOR_ADD, interceptor));
  }

  void dispatchRegisterStore(Store store) {
    Utils.checkNotNull(store);
    handler.sendMessage(handler.obtainMessage(STORE_REGISTER, store));
  }

  void dispatchUnRegisterStore(Store store) {
    Utils.checkNotNull(store);
    handler.sendMessage(handler.obtainMessage(STORE_UNREGISTER, store));
  }

  void performAddActionInterceptor(ActionInterceptor interceptor) {
    interceptorList.add(interceptor);
  }

  void performAction(Action action) {
    action.mark("start perform");
    action = decorateAction(action);
    boolean hit = false;
    for (Store store : storeList) {
      boolean temp = store.onAction(action);
      if (!hit && temp) {
        hit = true;
      }
    }
    if (hit) {
      action.mark("end perform");
    } else {
      action.mark("end perform:no Store to hit this action");
    }
  }

  public void performRegister(Store store) {
    Utils.checkNotNull(store);
    storeList.add(store);
  }

  public void performUnRegister(Store store) {
    Utils.checkNotNull(store);
    storeList.remove(store);
  }

  private Action decorateAction(Action action) {
    action.mark("start decorate ");
    for (ActionInterceptor interceptor : interceptorList) {
      action = interceptor.wrapAction(action);
    }
    action.mark("end decorate ");
    return action;
  }

  static class DispatcherHandler extends Handler {
    Dispatcher dispatcher;

    public DispatcherHandler(Looper looper, Dispatcher dispatcher) {
      super(looper);
      this.dispatcher = dispatcher;
    }

    @Override public void handleMessage(Message msg) {
      switch (msg.what) {
        case ACTION_INTERCEPTOR_ADD: {
          ActionInterceptor interceptor = (ActionInterceptor) msg.obj;
          dispatcher.performAddActionInterceptor(interceptor);
          break;
        }
        case ACTION_DISPATCH: {
          Action action = (Action) msg.obj;
          dispatcher.performAction(action);
          break;
        }
        case STORE_REGISTER: {
          Store store = (Store) msg.obj;
          dispatcher.performRegister(store);
          break;
        }
        case STORE_UNREGISTER: {
          Store store = (Store) msg.obj;
          dispatcher.performUnRegister(store);
          break;
        }
      }//switch end
    }
  }//class DispatcherHandler end
}
