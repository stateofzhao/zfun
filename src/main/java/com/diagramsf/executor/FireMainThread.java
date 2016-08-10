package com.diagramsf.executor;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by Diagrams on 2016/8/9 11:20
 */
public class FireMainThread implements FireThread {
  private Handler handler;

  public FireMainThread() {
    handler = new Handler(Looper.getMainLooper());
  }

  @Override public void post(Runnable runnable) {
    handler.post(runnable);
  }
}
