package com.diagramsf.lib.executor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 用线程池实现的{@link Executor}。<P>
 * 暂时在主线程回调{@link Task#onStateChange(int)}方法。<P>
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

  @Override public void execute(Task task, Object tag) {
    HolderRunnable runnable = new HolderRunnable(task, tag, this);
    dispatcher.dispatchSubmit(runnable);
  }

  /** 如果任务正在执行，不会取消任务，只是会把任务标记为取消状态，如果任务在队列中未运行，那么会直接取掉任务 */
  @Override public void cancel(Object tag) {
    dispatcher.dispatchCancel(tag);
  }

  /** 用来将{@link Task} 转换成{@link Runnable} ，并且持有task对应的tag */
  private static class HolderRunnable implements Runnable {
    Task task;
    ThreadExecutor scheduler;
    public Object tag;

    HolderRunnable(Task task, Object tag, ThreadExecutor scheduler) {
      this.task = task;
      this.scheduler = scheduler;
      this.tag = tag;
    }

    @Override public void run() {
      task.run();
    }
  }// end PriorityRunnable class

  //一个FutureTask 有七中状态，分别为：
  // NEW(0)：表示这是一个新的任务，或者还没有执行完的任务，是初始状态。
  // COMPLETING(1)：表示任务执行结束（正常执行结束，或者发生异常结束），但是还没有将结果保存到outcome中，是一个中间状态。
  // NORMAL(2)：表示任务正常执行结束，并且已经把执行结果保存到outcome字段中，是一个最终状态。
  // EXCEPTIONAL(3)：表示任务发生异常结束，异常信息已经保存到outcome中，这是一个最终状态。
  // CANCELLED(4)、
  // INTERRUPTING(5)、
  // INTERRUPTED(6)；
  //当调用FutureTask的cancel(boolean)方法时，会根据 FutureTask当前状态的不同而触发不同的行为：
  //1.如果当前state不是NEW（当任务正在正常执行时，状态是NEW） 那么就退出方法，返回false，
  // 这时的任务状态是 完成了 或是 被取消了 或是 被中断了。
  //2.如果当前state是NEW,那么如果cancel(boolean)中传递的是true（任务可中断），那么就会把状态置为INTERRUPTING
  // 并且在调用Thread.interrupt()方法后，把状态置为INTERRUPTED(NEW->INTERRUPTING->INTERRUPTED)；
  // 如果传递的是false，那么直接将状态置为CANCELLED（NEW->CANCELLED）。
  //
  //上述第一种情况是不会回调 FutureTask的 done()方法的，第二种情况一定会立即回调 FutureTask的 done()方法的（无论
  // 任务是否仍然正在执行！一个 FutureTask只会回调一次done()方法，所以执行完后不会再回调done()方法了）。
  private static class PriorityFuture extends FutureTask<Object> implements Comparable<PriorityFuture> {
    HolderRunnable holderRunnable;
    Task task;
    ThreadExecutor scheduler;

    PriorityFuture(HolderRunnable runnable, ThreadExecutor scheduler) {
      super(runnable, null);
      this.holderRunnable = runnable;
      this.task = runnable.task;
      this.scheduler = scheduler;
    }

    int getPriority() {
      return task.getPriority();
    }

    //这个方法无论如何都会被调用到的，我们在这个方法中发送任务执行结果通知。
    //这个方法不是在固定线程中执行的：
    //如果是在主线程调用了cancel()方法，如果任务被取消掉，那么执行任务的此方法就是在主线程中执行；
    //如果任务正常执行完毕，那么这个方法就是在执行任务的那个线程中调用。
    @Override protected void done() {
      try {
        //fixme 此处以后要直接有结果，可以通过修改Task类的run方法来实现返回结果，然后把Task封装成Callable即可
        //虽然是阻塞方法，但是无需担心，因为done()方法被调用的时机决定了此时FutureTask一定已经执行完毕或者被取消，
        // 所以get()方法一定是瞬间返回的。
        get();
        scheduler.dispatcher.dispatchFinish(holderRunnable);
      } catch (InterruptedException e) {
        e.printStackTrace();
        //执行线程被终止了，当调用了Future的cancel(true)方法，并且用户在run()/call()方法中处理了如何终止线程的情况下，会回调此方法
      } catch (ExecutionException e) {
        e.printStackTrace();
        //此处出现其它异常
      } catch (CancellationException e) {//如果被取消掉了，（会在调用cancel()方法的同一个线程中执行）
        e.printStackTrace();
        //用户直接取消了任务执行，此时任务应该还在队列中，没有被执行到，此时不必通过dispatcher来通知删除引用，
        // 因为在调cancel(tag)方法时，已经直接通过dispatcher进行了操作

        //在android系统AsyncTask中的实现是，在这里仍然会传递一个null结果到onResult()方法中
      }
    }

    // 当两个对象进行比较时，返回0代表它们相等；
    // 返回值<0（如例子中返回-1）代表this排在被比较对象之前；
    // 反之代表在被比较对象之后
    @Override public int compareTo(PriorityFuture another) {
      int priorityMe = getPriority();
      int priorityOther = another.task.getPriority();
      if (priorityMe == priorityOther) {
        return 0;
      } else if (priorityMe > priorityOther) {
        return -1;
      } else {
        return 1;
      }
    }
  }// end UseCaseFuture class

  /**
   * 制定ThreadPoolExecutor ，主要是要重写{@link ThreadPoolExecutor#submit(Runnable)} 方法，
   * 来返回自定义的Future，这样就可以通过自定义Future 来实现当队列是{@link PriorityBlockingQueue}时，
   * 能够按照优先级排队，同时能够通过重写Future的done()方法，来进行进一步的监听
   */
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
      runnable.task.onStateChange(Task.NEW);
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
            Task task = future.task;
            task.onStateChange(Task.CANCEL);//放到这里才能保证所有Task的onStateChange()都在同一个线程中回调
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
          if (f.task == holderRunnable.task) {
            futures.remove(f);
            f.task.onStateChange(Task.FINISHED);
            f.task.onStateChange(Task.DIE);
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
