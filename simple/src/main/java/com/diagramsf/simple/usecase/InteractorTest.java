package com.diagramsf.simple.usecase;

import com.diagramsf.lib.executor.BaseTask;

/**
 * Created by Diagrams on 2016/8/12 11:34
 */
public class InteractorTest extends BaseTask {
  private Callback<String, Exception> callback;

  public InteractorTest(Callback<String, Exception> callback) {
    this.callback = callback;
  }

  @Override public void run() {
    try {
      Thread.sleep(5000);
      if(isCancel()){
        return;
      }
      mMainHandler.post(new Runnable() {
        @Override public void run() {
          if(isCancel()){
            return;
          }
          callback.onResponse("请求成功");
        }
      });
    } catch (InterruptedException e) {
      e.printStackTrace();
      callback.onError(e);
    }
  }
}
