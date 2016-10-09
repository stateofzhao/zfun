package com.diagramsf.executor;

import com.google.common.base.Preconditions;

/**
 * 衔接{@link Interactor}和{@link ThreadExecutor}用的，是一个帮助类，为了进一步封装，对外暴露出友好的API。
 *
 * //也可以命名为UseCaseHandler，看个人喜好。
 *
 * Created by Diagrams on 2016/8/8 15:32
 */
public class InteractorHandler {
  private static volatile InteractorHandler singleton;

  private Executor executor;

  private InteractorHandler(Executor executor) {
    this.executor = executor;
  }

  public static InteractorHandler instance() {
    if (null == singleton) {
      synchronized (InteractorHandler.class) {
        if (null == singleton) {
          singleton = new InteractorHandler(new ThreadExecutor());
        }
      }
    }
    return singleton;
  }

  public void cancel(Object tag) {
    executor.cancel(tag);
  }

  public void execute(Interactor interactor, Object tag) {
    executor.execute(interactor, tag);
  }

  public InteractorBuilder prepareInteractor() {
    return new InteractorBuilder(this);
  }

  private static class InteractorBuilder {
    private InteractorHandler interactorHandler;

    private Object tag;
    private @Interactor.Priority int priority;

    InteractorBuilder(InteractorHandler interactorHandler) {
      this.interactorHandler = interactorHandler;
    }

    public InteractorBuilder priority(@Interactor.Priority int priority) {
      this.priority = priority;
      return this;
    }

    public InteractorBuilder tag(Object tag) {
      this.tag = tag;
      return this;
    }

    /**
     * 返回了对{@link Runnable}进行包装的{@link Interactor} ，这样就能够判断任务是否被取消了
     *
     * @return 返回了对 {@link Runnable}包装后的 {@link Interactor}
     */
    public Interactor execute(final Runnable runnable) {
      Preconditions.checkNotNull(runnable);
      if (null == tag) {
        tag = new Object();
      }
      if (priority <= 0) {
        priority = Interactor.NORMAL;
      }

      Interactor interactor = new Interactor() {
        private boolean cancel = false;
        private @Priority int priority;

        @Override public void run() {
          runnable.run();
        }

        @Override public void cancel() {
          cancel = true;
        }

        @Override public boolean isCancel() {
          return cancel;
        }

        @Override public int getPriority() {
          return priority;
        }

        @Override public void priority(@Priority int priority) {
          this.priority = priority;
        }

        @Override public void onStateChange(@State int state) {

        }
      };
      interactor.priority(priority);
      interactorHandler.execute(interactor, tag);
      return interactor;
    }
  }// end InteractorBuilder
}
