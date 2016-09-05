package com.diagramsf.learn.executor;

import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * 执行异步任务的线程池，暂不支持取消任务.
 * <p>
 * 调用 {@link #submitTaskForMainThreadCallback(Priority, Callable, ResultCallback)} 添加任务
 * <p>
 *
 * @deprecated 使用{@link UseCaseThreadPoolScheduler}代替，此类存在的用途就是用来学习线程池的~
 */
public class MyExecutor {
  private final static String TAG = "MyExecutor";

  /** 执行后台任务的优先级 */
  public enum Priority {
    LOW, NORMAL, HIGH
  }

  protected static final int CORE_THREAD = 5;
  protected static final int MAX_THREAD = 10;
  private static final int KEEP_TIME = 0;

  private static final int MESSAGE_POST_RESULT = 0x1;
  private static final int MESSAGE_POST_CANCEL = 0x2;
  private static final int MESSAGE_DO_CANCEL = 0x3;

  private static final AtomicInteger SEQUENCE_GENERATOR = new AtomicInteger();
  // 原子操作的整形数据,每次添加一个任务时会增加1，用来确定任务的先后顺序

  private static MyExecutor mSingleton;// 单例
  private static InternalHandler mInternalHandler;// 回调主线程的Handler
  private final ExecutorService mExecutor;// 线程池

  private final ReferenceQueue<ResultCallback> mReferenceQueue;// 弱引用回收队列

  // 暂时没用，后期用来取消请求用的
  private Map<WeakReference<Callable>, MyFutureTask> mTasks;

  /**
   * 执行完结果回调接口，在主线程中执行
   */
  public interface ResultCallback<T> {
    /**
     * 取消任务回调
     *
     * @param interrupt 是否是强制取消的（任务正在运行的时候取消了）true 是；false 不是（任务正在队列中，但是还没有来得及执行）
     */
    void onCancel(boolean interrupt);

    /**
     * 任务执行完毕
     *
     * @param result 任务执行结果
     */
    void onComplete(T result);
  }//class ResultCallback end

  static {
    mInternalHandler = new InternalHandler();
  }

  private MyExecutor() {
    mTasks = new HashMap<>();

    // 不能够被线程池处理时的额外处理者，例如
    // 当队列容量达到了上限但是仍然
    // 提交了任务，就会被它接受处理
    RejectedExecutionHandler defaultHandler = new Util.AbortPolicy();
    //线程池的阻塞队列
    PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();
    //实例化线程池
    mExecutor =
        new MyExecutorService(CORE_THREAD, MAX_THREAD, KEEP_TIME, TimeUnit.MILLISECONDS, queue,
            new Util.MyThreadFactory(), defaultHandler);

    mReferenceQueue = new ReferenceQueue<>();

    // 守护线程，用来清除 回调消失后对应的任务
    ClearThread clearThread = new ClearThread(mReferenceQueue);
    clearThread.start();
  }

  /** 单例 */
  public static MyExecutor newInstance() {
    if (null == mSingleton) {
      synchronized (MyExecutor.class) {
        if (null == mSingleton) {
          mSingleton = new MyExecutor();
        }
      }
    }
    return mSingleton;
  }

  /**
   * 提交要后台执行的任务,必须在主线程中调用！<br>
   * 由于对callback使用了弱引用，如果回调接口不是在android的Activity、Fragment或者Service中，有可能造成提交的任务不执行！<br>
   * 如果不需要回调，也可以传递 null！
   *
   * @param priority 任务优先级
   * @param task 具体要执行的任务
   * @param callback 任务回调接口,如果不需要回调传递null，如果需要回调
   * 必须是Activity、Fragment或者Service中的回调，否则有可能接受不到回调
   */
  public <T> void submitTaskForMainThreadCallback(Priority priority, Callable<T> task,
      ResultCallback<T> callback) {
    Util.checkMain();

    MyCallable<T> myCallable = new MyCallable<>(priority, task, callback, this);
    MyFutureTask<T> myTask = new MyFutureTask<>(myCallable);
    mTasks.put(new WeakReference<Callable>(task), myTask);
    mExecutor.execute(myTask);
  }

  public void cancelTask(Callable task) {
    Util.checkMain();
  }

  private static void postResult(MyCallable<?> myCallable) {
    Message msg = Message.obtain(mInternalHandler);
    msg.what = MESSAGE_POST_RESULT;
    msg.obj = myCallable;
    msg.sendToTarget();
  }

  private static void postCancel(MyCallable<?> myCallable) {
    Message msg = Message.obtain(mInternalHandler);
    msg.what = MESSAGE_POST_CANCEL;
    msg.obj = myCallable;
    msg.sendToTarget();
  }

  /**
   * 回调主线程来处理结果,这个类必须是静态的，防止内存泄漏
   * ,在这里聊一下内部静态类的好处，内部静态类不依赖于外部类，所以即使不释放内部静态类实例的引用 也不会造成外部类实例的不释放, 所以 在
   * {@link MyExecutor} 中，所有内部类都是 静态类。比如在
   * 本handler类中，由于需要主线程来处理接收到的消息，那么在未处理完时，Message中所有的引用都不会释放，但是
   * {@link MyExecutor}没有被强引用，它可以在不需要时 释放掉内存。
   * <p>
   * 加入了 对Callback的弱引用后，参照 PICASSO中的做法没有把 弱引用队列声明成
   * static，所以每个task在Handler中处理完后 才会不再引用 {@link MyExecutor}
   */
  final static class InternalHandler extends android.os.Handler {

    public InternalHandler() {
      super(Looper.getMainLooper());
    }

    @Override public void handleMessage(Message msg) {
      int what = msg.what;
      MyCallable myCallable = (MyCallable) msg.obj;
      if (null == myCallable) {
        return;
      }

      ResultCallbackWeakReference callback_ref = myCallable.callback_ref;
      ResultCallback callback = null;
      if (null != callback_ref) {
        callback = callback_ref.get();
      }

      switch (what) {
        case MESSAGE_POST_RESULT:
          if (null != callback) {
            callback.onComplete(myCallable.result);
          }
          break;
        case MESSAGE_POST_CANCEL:
          if (null != callback) {
            callback.onCancel(myCallable.isInterruptCancel);
          }
          break;
        case MESSAGE_DO_CANCEL:
          myCallable.myFutureTask.cancel(false);
          break;
        default:
          break;
      }
      // msg.recycle();
    }
  }

  /**
   * 自定义的线程池,最主要的就是为了实现 {@link ThreadPoolExecutor#submit(Callable)} 方法，
   * 来返回自定义的Future {@link MyFutureTask}，这样就能够在 线程池的PriorityBlockingQueue(优先级队列)
   * 中比较优先级了
   */
  private static class MyExecutorService extends ThreadPoolExecutor {

    public MyExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime,
        TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
        RejectedExecutionHandler handler) {
      super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @NonNull @Override public <T> Future<T> submit(Callable<T> task) {
      MyFutureTask<T> futureTask = new MyFutureTask<>((MyCallable<T>) task);
      execute(futureTask);
      return futureTask;
    }
  }

  /**
   * 提交到 线程池的PriorityBlockingQueue 中的任务, 泛型 T 表示任务执行完 生成的结果类型<br>
   * 实现了 Comparable接口 为了在 线程池的PriorityBlockingQueue(优先级队列) 中来比较优先级，
   * 并且重写了 done()方法来传递任务执行的结果
   */
  private static class MyFutureTask<T> extends FutureTask<T>
      implements Comparable<MyFutureTask<T>> {
    private MyCallable<T> myCallable;// 引用 MyCallable 是为了进行优先级比较

    public MyFutureTask(MyCallable<T> callable) {
      super(callable);

      this.myCallable = callable;
      myCallable.myFutureTask = this;
    }

    // 在这里需要注意下，当futureTask 标记为isDone状态时会调用这个方法，
    // cancel(boolean)掉FutureTask也会把FutureTask标记为isDone状态，
    // 也就是也会回调这个方法
    @Override protected void done() {
      ResultCallbackWeakReference callback_ref = myCallable.callback_ref;
      if (null == callback_ref) {// 没有回调接口，直接返回
        return;
      }
      // 不能够在这里这样做，因为android的垃圾回收线程是在主线程中执行的，但是这里不是在主线程中执行的！
      // if(null == callback_ref.get()){
      // return;
      // }

      // 在这里必须要加判断，来区分是否是cancel()掉的任务，因为调用cancel(boolean)后如果cancel成功也会回调
      // done()这个方法
      if (isCancelled()) {
        postCancel(myCallable);
        return;
      }
      try {
        myCallable.result = get();
        postResult(myCallable);
      } catch (InterruptedException e) {
        e.printStackTrace();
        myCallable.isInterruptCancel = true;
        postCancel(myCallable);
      } catch (ExecutionException e) {
        e.printStackTrace();
      } catch (CancellationException e) { // 当指定时长执行任务时，如果指定的时间到了，但是任务还没有执行完的话，会抛出这个异常
        Log.d(TAG, "something is wrong reason: " + e.getCause());
        postResult(null);
      } finally {// 释放不需要的内存，这里应该释放所有对 该对象的引用
        myCallable = null;// 执行完毕后，不需要再引用
        // 根据java面向对象原理，它不应该来管它自己以外的事，而且这里也不能够这样做，因为MyCallable还需要根据它来判断任务是否执行完，是否取消等的状态
        // myCallable.myFutureTask = null;//
        // 因为myCallable还要传递给主线程的Handler，但是该对象已经不需要了，所以把myCallable引用的该对象置为null
      }
    }

    @Override public int compareTo(@NonNull MyFutureTask<T> another) {
      Priority p1 = myCallable.getPriority();
      Priority p2 = another.myCallable.getPriority();

      // 当两个对象进行比较时，返回0代表它们相等；
      // 返回值<0（如例子中返回-1）代表this排在被比较对象之前；
      // 反之代表在被比较对象之后
      return (p1 == p2 ? myCallable.getSequence() - another.myCallable.getSequence()
          : p2.ordinal() - p1.ordinal());
    }
  }//end MyFutureTask class

  /**
   * 具体执行任务的 Callable ,泛型 T 表示任务执行完生成的结果类型 <br>
   * <p>
   * 把通过 {@link MyExecutor#submitTaskForMainThreadCallback(Priority, Callable, ResultCallback)}
   * 传递的任务，重新封装一下，方便内部处理.
   */
  private static class MyCallable<T> implements Callable<T> {
    private Priority priority;
    private int sequence;

    private WeakReference<Callable<T>> realTask_ref;

    public ResultCallbackWeakReference callback_ref;

    public T result;

    public boolean isInterruptCancel = false;

    // 引用 MyFutureTask是为了 取消任务 以及 判断请求的任务是否执完毕，是否取消等
    public MyFutureTask<T> myFutureTask;

    public MyCallable(Priority priority, Callable<T> real, ResultCallback<T> callback,
        MyExecutor myExecutor) {
      this.priority = priority;
      sequence = SEQUENCE_GENERATOR.incrementAndGet();

      realTask_ref = new WeakReference<>(real);

      if (null != callback) {
        this.callback_ref =
            new ResultCallbackWeakReference(this, callback, myExecutor.mReferenceQueue);
      }
    }

    public Priority getPriority() {
      return priority;
    }

    public int getSequence() {
      return sequence;
    }

    @Override public T call() throws Exception {
      //如果原task已经被释放了，标记本Future为cancel状态。
      Callable<T> real = realTask_ref.get();
      if (null == real) {
        if (null != myFutureTask) {
          myFutureTask.cancel(false);
        }
        return null;
      }

      T result = null;
      try {
        result = real.call();
      } finally {
        Thread.currentThread().setName(Util.THREAD_NAME);
      }
      return result;
    }
  }

  /***
   * 弱引用，当弱引用的对象释放后，这个弱引用会加入到一个ReferenceQueue 中（构造函数传入的q参数），然后遍历此队列取出不需要的
   * {@link ResultCallbackWeakReference#myCallable},cancel掉任务，达到自动检测 取消不需要的任务。
   */
  final static class ResultCallbackWeakReference extends WeakReference<ResultCallback> {
    MyCallable myCallable; // 弱引用中 额外缓存的字段，当本弱引用类引用的对象被回收后，用来清理资源的。

    //这里需要说明下，ReferenceQueue<T>的泛型需要跟 放入到其中的 引用的泛型保持一致，例如这里的 弱引用的泛型就是ResultCallback，
    // 那么 ReferenceQueue的泛型也是ResultCallback。
    //但是通常情况下，如果自定义的弱引用是公共的（希望让其他人使用）那么就需要让ReferenceQueue的泛型类型扩大，
    // 不要直接写死一个类型，那么此时就需要设置泛型的下界了，在本例类中需要这么写ReferenceQueue<? super ResultCallback>
    public ResultCallbackWeakReference(MyCallable myCallable, ResultCallback r,
        ReferenceQueue<ResultCallback> q) {
      super(r, q);
      this.myCallable = myCallable;
    }
  }//end CallbackWeakReference class

  /** 守护线程,当主线程结束后自己会结束掉 */
  final static class ClearThread extends Thread {
    ReferenceQueue<ResultCallback> referenceQueue;

    public ClearThread(ReferenceQueue<ResultCallback> referenceQueue) {

      this.referenceQueue = referenceQueue;
      setDaemon(true);
    }

    @Override public void run() {
      Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND);
      while (true) {
        try {
          ResultCallbackWeakReference ref = (ResultCallbackWeakReference) referenceQueue.remove();// 这个是阻塞方法，当没有值时，会让线程wait()
          Message msg = mInternalHandler.obtainMessage();
          msg.what = MESSAGE_DO_CANCEL;
          msg.obj = ref.myCallable;
          msg.sendToTarget();
        } catch (InterruptedException e) {// 线程被终止，应该是JVM完全退出了
          e.printStackTrace();
          break;
        } catch (final Exception e) {// 有其它异常，就是致命的，因为会造成内存溢出,所以这里直接抛出
          // RuntimeException
          Log.e(TAG, "线程池出现致命异常");
          mInternalHandler.post(new Runnable() {
            @Override public void run() {
              throw new RuntimeException(e);
            }
          });
          break;
        }
      }
    }
  }//end ClearThread class

  // Comparator 是一种策略模式（strategy design pattern），在外部定义比较方式，比较灵活
  // private class MyFutureTaskCompare implements
  // Comparator<MyFutureTaskCompare> {
  //
  // @Override
  // public int compare(MyFutureTaskCompare lhs, MyFutureTaskCompare rhs) {
  // return 0;
  // }
  //
  // }
}//class MyExecutor end
