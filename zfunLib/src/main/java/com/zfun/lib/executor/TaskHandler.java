package com.zfun.lib.executor;

import com.google.common.base.Preconditions;

/**
 * 衔接{@link Task}和{@link ThreadExecutor}用的，是一个帮助类，为了进一步封装，对外暴露出友好的API。
 * <p>
 * Created by zfun on 2016/8/8 15:32
 */
public class TaskHandler {
  private static volatile TaskHandler singleton;

  private Executor executor;

  interface Callback {
    /** @like {@link Task#onStateChange(int)} */
    void onStateChange(@Task.State int state);
  }

  private TaskHandler(Executor executor) {
    this.executor = executor;
  }

  public static TaskHandler instance() {
    if (null == singleton) {
      synchronized (TaskHandler.class) {
        if (null == singleton) {
          singleton = new TaskHandler(new ThreadExecutor());
        }
      }
    }
    return singleton;
  }

  public void cancel(Object tag) {
    executor.cancel(tag);
  }

  public void execute(Task task, Object tag) {
    executor.execute(task, tag);
  }

  public void execute(Runnable task, Object tag) {
    prepare().tag(tag).execute(task);
  }

  public TaskBuilder prepare() {
    return new TaskBuilder(this);
  }

  //builder
  private static class TaskBuilder {
    private TaskHandler taskHandler;

    private Object tag;
    private Callback callback;
    private @Task.Priority int priority;

    TaskBuilder(TaskHandler taskHandler) {
      this.taskHandler = taskHandler;
    }

    public TaskBuilder priority(@Task.Priority int priority) {
      this.priority = priority;
      return this;
    }

    public TaskBuilder tag(Object tag) {
      this.tag = tag;
      return this;
    }

    public TaskBuilder callback(Callback callback) {
      this.callback = callback;
      return this;
    }

    public void execute(final Runnable runnable) {
      Preconditions.checkNotNull(runnable);
      if (null == tag) {
        tag = new Object();
      }
      if (priority <= 0) {
        priority = Task.NORMAL;
      }

      Task task = new Task() {
        private Callback callback = TaskBuilder.this.callback;
        @Override public void run() {
          runnable.run();
        }

        @Override public int getPriority() {
          return priority;
        }

        @Override public void onStateChange(@State int state) {
          if (null != callback) {
            callback.onStateChange(state);
          }
        }
      };
      taskHandler.execute(task, tag);
    }//method
  }// end InteractorBuilder

}//end class
