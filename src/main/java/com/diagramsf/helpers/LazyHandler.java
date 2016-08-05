package com.diagramsf.helpers;

import android.os.Handler;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;

/**
 * 懒加载Handler，只有在{@link #resume()}后才会执行{@link #post(Runnable)}中传递的Runnable，在{@link #pause()}
 * 后暂停执行{@link #post(Runnable)}中传递的Runnable，{@link #destory()}后销毁所有Runnable。
 *
 * Created by Diagrams on 2016/6/30 13:49
 */
public class LazyHandler {
  private Handler mHandler;
  private List<StateRunnable> mRunnableList;

  private boolean isResume;

  public LazyHandler(Handler handler) {
    Preconditions.checkNotNull(handler);
    mHandler = handler;
    mRunnableList = new ArrayList<>();
  }

  /** 继续Handler中Runnable的执行 */
  public synchronized void resume() {
    isResume = true;
    removeFinishedRunnable();
    for (int i = mRunnableList.size() - 1; i >= 0; i--) {
      handlerPost(mRunnableList.get(i));
    }
  }

  /** 暂停Handler中Runnable的执行 */
  public synchronized void pause() {
    isResume = false;
    handlerRemoveAll();
  }

  public synchronized void destory() {
    handlerRemoveAll();
  }

  public synchronized void post(Runnable runnable) {
    Preconditions.checkNotNull(runnable);
    StateRunnable stateRunnable = new StateRunnable(runnable);
    mRunnableList.add(stateRunnable);
    if (isResume) {//可以直接运行
      handlerPost(stateRunnable);
    }
  }

  /** 在Handler中执行任务 */
  private void handlerPost(Runnable runnable) {
    mHandler.post(runnable);
  }

  /** 从Handler中移除所有等待执行的任务 */
  private void handlerRemoveAll() {
    mHandler.removeCallbacksAndMessages(null);
  }

  private void removeFinishedRunnable() {
    for (int i = mRunnableList.size() - 1; i >= 0; i--) {
      StateRunnable stateRunnable = mRunnableList.get(i);
      if (stateRunnable.state == StateRunnable.STATE_FINISHED) {
        mRunnableList.remove(stateRunnable);
      }
    }
  }

  private static class StateRunnable implements Runnable {
    private static final int STATE_NEW = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_FINISHED = 2;

    private Runnable runnable;
    private int state;

    public StateRunnable(Runnable runnable) {
      this.runnable = runnable;
      state = STATE_NEW;
    }

    public int getState() {
      return state;
    }

    @Override public void run() {
      state = STATE_RUNNING;
      runnable.run();
      state = STATE_FINISHED;
    }
  }//class end
}
