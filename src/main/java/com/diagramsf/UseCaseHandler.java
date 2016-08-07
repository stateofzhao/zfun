package com.diagramsf;

import android.support.annotation.NonNull;

import com.diagramsf.domain.UseCase;
import java.util.concurrent.Callable;

/**
 * 处理{@link UseCase}
 * <p>
 * Created by Diagrams on 2016/6/27 11:52
 */
public class UseCaseHandler {
  private static volatile UseCaseHandler singleton;

  private UseCaseScheduler scheduler;

  private UseCaseHandler() {
  }

  private void setUseCaseScheduler(UseCaseScheduler scheduler) {
    this.scheduler = scheduler;
  }

  public static UseCaseHandler instance() {
    if (null == singleton) {
      synchronized (UseCaseHandler.class) {
        if (null == singleton) {
          singleton = new UseCaseHandler();
          singleton.setUseCaseScheduler(new UseCaseThreadPoolScheduler());
        }
      }
    }
    return singleton;
  }

  public <T extends UseCase.RequestValue> UseCaseDecorator request(T requestValue) {
    return new UseCaseDecorator(requestValue, this);
  }

  public void cancel(Object tag) {
    scheduler.cancel(tag);
  }

  private void execute(UseCase useCase) {
    scheduler.execute(useCase);
  }

  private <T extends UseCase.ResponseValue> void notifyResponse(T responseValue,
      @NonNull UseCase.Listener<T> listener) {
    scheduler.notifyResult(responseValue, listener);
  }

  private <E extends UseCase.ErrorValue> void error(E errorValue,
      @NonNull UseCase.ErrorListener<E> listener) {
    scheduler.error(errorValue, listener);
  }

  private static class ResultListenerWrapper<V extends UseCase.ResponseValue>
      implements UseCase.Listener<V> {
    UseCaseHandler handler;
    UseCase.Listener<V> listener;
    UseCase useCase;

    public ResultListenerWrapper(UseCase useCase, UseCaseHandler handler,
        UseCase.Listener<V> listener) {
      this.handler = handler;
      this.listener = listener;
      this.useCase = useCase;
    }

    @Override public void onSucceed(V response) {
      if (null != listener && !useCase.isCancel()) {
        handler.notifyResponse(response, listener);
      }
    }
  }//class ResultListenerWrapper end

  private static class ErrorListenerWrapper<E extends UseCase.ErrorValue>
      implements UseCase.ErrorListener<E> {
    UseCaseHandler handler;
    UseCase.ErrorListener<E> errorListener;
    UseCase useCase;

    public ErrorListenerWrapper(UseCase useCase, UseCaseHandler handler,
        UseCase.ErrorListener<E> errorListener) {
      this.handler = handler;
      this.errorListener = errorListener;
      this.useCase = useCase;
    }

    @Override public void onError(E error) {
      if (null != errorListener && !useCase.isCancel()) {
        handler.error(error, errorListener);
      }
    }
  }// class ErrorListenerWrapper end

  public static class UseCaseDecorator {
    private UseCase.ErrorListener errorListener;
    private UseCase.Listener listener;
    private UseCase.RequestValue requestValue;
    private Object tag;
    private int priority = UseCase.NORMAL;

    private UseCaseHandler handler;

    public UseCaseDecorator(UseCase.RequestValue requestValue, UseCaseHandler handler) {
      this.requestValue = requestValue;
      this.handler = handler;
    }

    public UseCaseDecorator error(UseCase.ErrorListener listener) {
      errorListener = listener;
      return this;
    }

    public UseCaseDecorator listener(UseCase.Listener listener) {
      this.listener = listener;
      return this;
    }

    public UseCaseDecorator tag(Object tag) {
      this.tag = tag;
      return this;
    }

    public UseCaseDecorator priority(@UseCase.Type int priority) {
      this.priority = priority;
      return this;
    }

    public void execute(@NonNull final Runnable runnable) {
      UseCase useCase = new UseCase() {
        @Override public void execute(RequestValue requestValue) {
          runnable.run();
        }
      };
      useCase.justRun();
      decoratorUseCase(useCase);
      handler.execute(useCase);
    }

    public <V extends UseCase.ResponseValue> void execute(@NonNull final Callable<V> runnable) {
      UseCase useCase = new UseCase() {
        @Override public void execute(RequestValue requestValue) {
          try {
            V result = runnable.call();
            getListener().onSucceed(result);
          } catch (Exception e) {
            e.printStackTrace();
            ErrorValue errorValue = new ErrorValue() {
              public Exception e;

              @Override public void setException(Exception e) {
                this.e = e;
              }
            };
            getErrorListener().onError(errorValue);
          }
        }
      };
      useCase.justRun();
      decoratorUseCase(useCase);
      handler.execute(useCase);
    }

    public void execute(@NonNull UseCase useCase) {
      decoratorUseCase(useCase);
      handler.execute(useCase);
    }

    private void decoratorUseCase(UseCase useCase) {
      useCase.setRequestValue(requestValue);
      useCase.setTag(tag);
      useCase.setListener(new ResultListenerWrapper<>(useCase, handler, listener));
      useCase.setErrorListener(new ErrorListenerWrapper<>(useCase, handler, errorListener));
      useCase.setPriority(priority);
    }
  } // class UseCaseDecorator end
}
