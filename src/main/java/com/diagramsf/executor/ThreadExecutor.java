package com.diagramsf.executor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 用线程池实现的{@link Executor}。
 *
 * //也可以命名为UseCaseThreadPoolScheduler，看个人喜好。
 *
 * Created by Diagrams on 2016/8/8 11:33
 */
class ThreadExecutor implements Executor {
  private static final int CORE_SIZE = 2;
  private static final int MAX_SIZE = 4;
  private static final long TIMEOUT = 30;

  private static final int ADD = 0X1;
  private static final int CANCEL = 0X2;
  private static final int POST_FINISHED = 0X5;

  private Dispatcher dispatcher;

  ThreadExecutor() {
    PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();
    ExecutorService service =
        new MyThreadPoolExecutor(CORE_SIZE, MAX_SIZE, TIMEOUT, TimeUnit.SECONDS, queue);
    dispatcher = new Dispatcher(service);
  }

  @Override public void execute(Interactor interactor, Object tag) {
    HolderRunnable runnable = new HolderRunnable(interactor, tag, this);
    dispatcher.dispatchSubmit(runnable);
  }

  /** 如果任务正在执行，不会取消任务，只是会把任务标记为取消状态，如果任务在队列中未运行，那么会直接取掉任务 */
  @Override public void cancel(Object tag) {
    dispatcher.dispatchCancel(tag);
  }

  private static class HolderRunnable implements Runnable {
    Interactor interactor;
    ThreadExecutor scheduler;
    public Object tag;

    HolderRunnable(Interactor interactor, Object tag, ThreadExecutor scheduler) {
      this.interactor = interactor;
      this.scheduler = scheduler;
      this.tag = tag;
    }

    @Override public void run() {
      interactor.run();
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
  private static class PriorityFuture extends FutureTask<Object>
      implements Comparable<PriorityFuture> {
    HolderRunnable holderRunnable;
    Interactor interactor;
    ThreadExecutor scheduler;

    PriorityFuture(HolderRunnable runnable, ThreadExecutor scheduler) {
      super(runnable, null);
      this.holderRunnable = runnable;
      this.interactor = runnable.interactor;
      this.scheduler = scheduler;
    }

    int getPriority() {
      return interactor.getPriority();
    }

    //这个方法，不是在固定线程中执行的，如果是在主线程调用了cancel()方法，如果任务被取消掉，
    // 那么执行任务的此方法就是在主线程中执行；
    //如果任务正常执行完毕，那么这个方法就是在执行任务的那个线程中调用。
    @Override protected void done() {
      if (isCancelled()) {//如果被取消掉了，（会在调用cancel()方法的同一个线程中执行）
        interactor.cancel();//取消掉UseCase的回调
      } else {//正常执行完任务后，需要把任务从mFutureMap中移除，防止内存泄露
        if (!interactor.isCancel()) {
          scheduler.dispatcher.dispatchFinish(holderRunnable);
        }
      }
    }

    // 当两个对象进行比较时，返回0代表它们相等；
    // 返回值<0（如例子中返回-1）代表this排在被比较对象之前；
    // 反之代表在被比较对象之后
    @Override public int compareTo(PriorityFuture another) {
      int priorityMe = getPriority();
      int priorityOther = another.interactor.getPriority();
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

    MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue) {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override public Future<?> submit(Runnable task) {
      HolderRunnable holderRunnable = (HolderRunnable) task;
      PriorityFuture ftask = new PriorityFuture(holderRunnable, holderRunnable.scheduler);
      execute(ftask);
      return ftask;
    }
  }// end MyThreadPoolExecutor class

  private static class DispatcherHandler extends Handler {
    private Dispatcher dispatcher;

    DispatcherHandler(Dispatcher dispatcher) {
      super(Looper.getMainLooper());
      this.dispatcher = dispatcher;
    }

    @Override public void handleMessage(Message msg) {
      int what = msg.what;
      if (what == ADD) {
        HolderRunnable runnable = (HolderRunnable) msg.obj;
        dispatcher.performSubmit(runnable);
      } else if (what == CANCEL) {
        dispatcher.performCancel(msg.obj);
      } else if (what == POST_FINISHED) {
        HolderRunnable runnable = (HolderRunnable) msg.obj;
        dispatcher.performFinishTask(runnable);
      }
    }
  }// end InternalHandler

  private static class Dispatcher {
    private ExecutorService service;
    private DispatcherHandler handler;//主线程Handler
    private Map<Object, List<PriorityFuture>> futureMap;

    private Dispatcher(ExecutorService service) {
      this.service = service;
      futureMap = new HashMap<>();
      handler = new DispatcherHandler(this);
    }

    void dispatchSubmit(HolderRunnable holderRunnable) {
      Message msg = Message.obtain(handler);
      msg.what = ADD;
      msg.obj = holderRunnable;
      msg.sendToTarget();
    }

    void dispatchCancel(Object tag) {
      Message msg = Message.obtain(handler);
      msg.what = CANCEL;
      msg.obj = tag;
      msg.sendToTarget();
    }

    void dispatchFinish(HolderRunnable holderRunnable) {
      Message msg = Message.obtain(handler);
      msg.what = POST_FINISHED;
      msg.obj = holderRunnable;
      msg.sendToTarget();
    }

    void performSubmit(HolderRunnable runnable) {
      runnable.interactor.onStateChange(Interactor.NEW);
      Object tag = runnable.tag;
      PriorityFuture future = (PriorityFuture) service.submit(runnable);
      if (null != tag) {
        List<PriorityFuture> futures = futureMap.get(tag);
        if (null == futures) {
          futures = new ArrayList<>();
        }
        if (!future.isDone()) {
          futures.add(future);
          futureMap.put(tag, futures);
        }
      }
    }

    void performCancel(Object tag) {
      if (null != tag) {
        List<PriorityFuture> futures = futureMap.remove(tag);
        if (null != futures) {
          for (PriorityFuture future : futures) {
            //一旦cancel(boolean)可以被取消（返回true），那么会在返回 true之前回调 FutureTask的 done()方法！
            future.cancel(false);
            Interactor interactor = future.interactor;
            interactor.cancel();
            interactor.onStateChange(Interactor.CANCEL);
          }
        }
      }
    }

    void performFinishTask(HolderRunnable holderRunnable) {
      final Object tag = holderRunnable.tag;
      if (null != tag) {
        List<PriorityFuture> futures = futureMap.get(tag);
        for (int i = futures.size() - 1; i >= 0; i--) {
          PriorityFuture f = futures.get(i);
          if (f.interactor == holderRunnable.interactor) {
            futures.remove(f);
            f.interactor.onStateChange(Interactor.FINISHED);
            f.interactor.onStateChange(Interactor.DIE);
            break;
          }
        }
        if (futures.size() == 0) {
          futureMap.remove(tag);
        }
      }
    }
  }// end Dispatcher
}// end ThreadExecutor
