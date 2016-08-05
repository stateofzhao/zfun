package com.diagramsf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Diagrams on 2016/6/27 12:00
 */
public class UseCaseThreadPoolScheduler implements UseCaseScheduler {
  protected static final int CORE_SIZE = 2;
  protected static final int MAX_SIZE = 4;
  protected static final long TIMEOUT = 30;

  private static final int ADD = 0X1;
  private static final int CANCEL = 0X2;
  private static final int POST_RESULT = 0X3;
  private static final int POST_ERROR = 0X4;
  private static final int POST_FINISHED = 0X5;

  private InternalHandler mMainHandler;//主线程Handler

  private ExecutorService mExecutorService;

  private Map<Object, List<UseCaseFuture>> mFutureMap;

  public UseCaseThreadPoolScheduler() {
    mFutureMap = new HashMap<>();
    PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();
    mExecutorService =
        new MyThreadPoolExecutor(CORE_SIZE, MAX_SIZE, TIMEOUT, TimeUnit.SECONDS, queue);
    mMainHandler = new InternalHandler(this);
  }

  @Override public void execute(UseCase useCase) {
    PriorityRunnable runnable = new PriorityRunnable(useCase, this);
    Message msg = Message.obtain(mMainHandler);
    msg.what = ADD;
    msg.obj = runnable;
    msg.sendToTarget();
  }

  @Override public void cancel(Object tag) {
    Message msg = Message.obtain(mMainHandler);
    msg.what = CANCEL;
    msg.obj = tag;
    msg.sendToTarget();
  }

  @Override public <T extends UseCase.ResponseValue> void notifyResult(final T response,
      final UseCase.Listener<T> listener) {
    Message msg = Message.obtain(mMainHandler);
    msg.what = POST_RESULT;
    msg.obj = new Runnable() {
      @Override public void run() {
        if (null != listener) {
          listener.onSucceed(response);
        }
      }
    };
    msg.sendToTarget();
  }

  @Override public <E extends UseCase.ErrorValue> void error(final E error,
      final UseCase.ErrorListener<E> errorListener) {
    Message msg = Message.obtain(mMainHandler);
    msg.what = POST_ERROR;
    msg.obj = new Runnable() {
      @Override public void run() {
        errorListener.onError(error);
      }
    };
    msg.sendToTarget();
  }

  private void performSubmit(PriorityRunnable runnable, Object tag) {
    UseCaseFuture future = (UseCaseFuture) mExecutorService.submit(runnable);
    if (null != tag) {
      List<UseCaseFuture> futures = mFutureMap.get(tag);
      if (null == futures) {
        futures = new ArrayList<>();
      }
      if (!future.isDone()) {
        futures.add(future);
        mFutureMap.put(tag, futures);
      }
    }
  }

  private void performCancel(Object tag) {
    if (null != tag) {
      List<UseCaseFuture> futures = mFutureMap.remove(tag);
      if (null != futures) {
        for (UseCaseFuture future : futures) {
          //一旦cancel(boolean)可以被取消（返回true），那么会在返回 true之前回调 FutureTask的 done()方法！
          if (!future.cancel(false)) {
            UseCase useCase = future.useCase;
            useCase.cancel();
          }
        }
      }
    }
  }

  private void performFinishTask(UseCase useCase) {
    final Object tag = useCase.getTag();
    if (null != tag) {
      List<UseCaseFuture> futures = mFutureMap.get(tag);
      for (int i = futures.size() - 1; i >= 0; i--) {
        UseCaseFuture f = futures.get(i);
        if (f.useCase == useCase) {
          futures.remove(f);
          break;
        }
      }
      if (futures.size() == 0) {
        mFutureMap.remove(tag);
      }
    }

    //如果只是运行的Runnable，不需要将结果传递回去，那么这里也会给出一个回调，但是会传递null，这样
    //请求者也可以根据这个回调来获得任务运行完了的通知
    if (useCase.isJustRun()) {
      useCase.getListener().onSucceed(null);
    }
  }

  private void performPostResult(Runnable runnable) {
    runnable.run();
  }

  private void performPostError(Runnable runnable) {
    runnable.run();
  }

  private static class PriorityRunnable implements Runnable {
    public UseCase useCase;
    public UseCaseThreadPoolScheduler scheduler;

    public PriorityRunnable(UseCase useCase, UseCaseThreadPoolScheduler scheduler) {
      this.useCase = useCase;
      this.scheduler = scheduler;
    }

    @Override public void run() {
      useCase.run();
    }
  }// end PriorityRunnable class

  //一个FutureTask 有七中状态，分别为：
  // NEW(0)、COMPLETING(1)、NORMAL(2)、EXCEPTIONAL(3)、CANCELLED(4)、INTERRUPTING(5)、INTERRUPTED(6)；
  //当调用FutureTask的cancel(boolean)方法时，会根据 FutureTask当前状态的不同而触发不同的行为：
  //1.如果当前state不是NEW（当任务正在正常执行时，状态是NEW） 那么就退出方法，返回false，
  // 这时的任务状态是 完成了 或是被取消了 或是被中断了。
  //2.如果当前state是NEW,那么如果cancel(boolean)中传递的是true（任务可中断），那么就会把状态置为INTERRUPTING
  // 并且在调用Thread.interrupt()方法后，把状态置为INTERRUPTED(NEW->INTERRUPTING->INTERRUPTED)；
  // 如果传递的是false，那么直接将状态置为CANCELLED（NEW->CANCELLED）。
  //
  //上述第一种情况是不会回调 FutureTask的 done()方法的，第二种情况一定会立即回调 FutureTask的 done()方法的（无论
  // 任务是否仍然正在执行！一个 FutureTask只会回调一次done()方法，所以执行完后不会再回调done()方法了）。
  private static class UseCaseFuture extends FutureTask<Object>
      implements Comparable<UseCaseFuture> {
    UseCase useCase;
    UseCaseThreadPoolScheduler scheduler;

    public UseCaseFuture(Runnable runnable, UseCase useCase, UseCaseThreadPoolScheduler scheduler) {
      super(runnable, null);
      this.useCase = useCase;
      this.scheduler = scheduler;
    }

    public int getPriority() {
      return useCase.getPriority();
    }

    //这个方法，不是在固定线程中执行的，如果是在主线程调用了cancel()方法，如果任务被取消掉，
    // 那么执行任务的此方法就是在主线程中执行；
    //如果任务正常执行完毕，那么这个方法就是在执行任务的那个线程中调用。
    @Override protected void done() {
      if (isCancelled()) {//如果被取消掉了，（会在调用cancel()方法的同一个线程中执行）
        useCase.cancel();//取消掉UseCase的回调
      } else {//正常执行完任务后，需要把任务从mFutureMap中移除，防止内存泄露
        if (!useCase.isCancel()) {
          Message msg = Message.obtain(scheduler.mMainHandler);
          msg.what = POST_FINISHED;
          msg.obj = useCase;
          msg.sendToTarget();
        }
      }
    }

    // 当两个对象进行比较时，返回0代表它们相等；
    // 返回值<0（如例子中返回-1）代表this排在被比较对象之前；
    // 反之代表在被比较对象之后
    @Override public int compareTo(UseCaseFuture another) {
      int priorityMe = getPriority();
      int priorityOther = another.useCase.getPriority();
      if (priorityMe == priorityOther) {
        return 0;
      } else if (priorityMe > priorityOther) {
        return -1;
      } else {
        return 1;
      }
    }
  }// end UseCaseFuture class

  private static class MyThreadPoolExecutor extends ThreadPoolExecutor {

    public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
        TimeUnit unit, BlockingQueue<Runnable> workQueue) {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override public Future<?> submit(Runnable task) {
      PriorityRunnable priorityRunnable = (PriorityRunnable) task;
      UseCaseFuture ftask =
          new UseCaseFuture(task, priorityRunnable.useCase, priorityRunnable.scheduler);
      execute(ftask);
      return ftask;
    }
  }// end MyThreadPoolExecutor class

  private static class InternalHandler extends Handler {
    private UseCaseThreadPoolScheduler scheduler;

    public InternalHandler(UseCaseThreadPoolScheduler scheduler) {
      super(Looper.getMainLooper());
      this.scheduler = scheduler;
    }

    @Override public void handleMessage(Message msg) {
      int what = msg.what;
      if (what == ADD) {
        PriorityRunnable runnable = (PriorityRunnable) msg.obj;
        scheduler.performSubmit(runnable, runnable.useCase.getTag());
      } else if (what == CANCEL) {
        scheduler.performCancel(msg.obj);
      } else if (what == POST_RESULT) {
        Runnable runnable = (Runnable) msg.obj;
        scheduler.performPostResult(runnable);
      } else if (what == POST_ERROR) {
        Runnable runnable = (Runnable) msg.obj;
        scheduler.performPostError(runnable);
      } else if (what == POST_FINISHED) {
        scheduler.performFinishTask((UseCase) msg.obj);
      }
    }
  }// end InternalHandler
}
