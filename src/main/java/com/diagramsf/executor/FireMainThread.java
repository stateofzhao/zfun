package com.diagramsf.executor;

import android.os.Handler;
import android.os.Looper;

/**
 * 传递到主线程
 *
 * Created by Diagrams on 2016/8/9 11:20
 */
public class FireMainThread implements FireThread {
  private Handler handler;
  private Interactor interactor;

  public FireMainThread(Interactor interactor) {
    handler = new Handler(Looper.getMainLooper());
    this.interactor = interactor;
  }

  @Override public void post(Runnable runnable) {
    if (null != interactor && !interactor.isCancel()) {
      handler.post(runnable);
    }
  }
}
