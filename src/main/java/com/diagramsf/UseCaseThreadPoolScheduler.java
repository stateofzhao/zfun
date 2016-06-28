package com.diagramsf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.*;
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

    private InternalHandler mMainHandler;//主线程Handler

    private ExecutorService mExecutorService;

    private Map<Object, List<UseCaseFuture>> mFutureMap;

    public UseCaseThreadPoolScheduler() {
        mFutureMap = new HashMap<>();
        PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();
        mExecutorService = new MyThreadPoolExecutor(CORE_SIZE, MAX_SIZE, TIMEOUT, TimeUnit.SECONDS, queue);
        mMainHandler = new InternalHandler(this);
    }

    @Override
    public void execute(UseCase useCase) {
        PriorityRunnable runnable = new PriorityRunnable(useCase,this);
        Message msg = Message.obtain(mMainHandler);
        msg.what = ADD;
        msg.obj = runnable;
        msg.sendToTarget();
    }

    @Override
    public void cancel(Object tag) {
        Message msg = Message.obtain(mMainHandler);
        msg.what = CANCEL;
        msg.obj = tag;
        msg.sendToTarget();
    }

    @Override
    public <T extends UseCase.ResponseValue> void notifyResult(final T response, final UseCase.Listener<T> listener) {
        Message msg = Message.obtain(mMainHandler);
        msg.what = POST_RESULT;
        msg.obj = new Runnable() {
            @Override
            public void run() {
                if (null != listener) {
                    listener.onSucceed(response);
                }

            }
        };
        msg.sendToTarget();

    }

    @Override
    public <E extends UseCase.ErrorValue> void error(final E error, final UseCase.ErrorListener<E> errorListener) {
        Message msg = Message.obtain(mMainHandler);
        msg.what = POST_ERROR;
        msg.obj = new Runnable() {
            @Override
            public void run() {
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
            futures.add(future);
            mFutureMap.put(tag, futures);
        }
    }

    private void performCancel(Object tag) {
        if (null != tag) {
            List<UseCaseFuture> futures = mFutureMap.remove(tag);
            if (null != futures) {
                for (UseCaseFuture future : futures) {
                    if (!future.cancel(false)) {
                        UseCase useCase = future.useCase;
                        useCase.cancel();
                    }
                }
            }
        }
    }

    private static class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {
        public int priority;
        public UseCase useCase;
        public UseCaseThreadPoolScheduler scheduler;

        public PriorityRunnable(UseCase useCase,UseCaseThreadPoolScheduler scheduler) {
            this.useCase = useCase;
            this.priority = useCase.getPriority();
            this.scheduler = scheduler;
        }

        public int getPriority() {
            return priority;
        }

        // 当两个对象进行比较时，返回0代表它们相等；
        // 返回值<0（如例子中返回-1）代表this排在被比较对象之前；
        // 反之代表在被比较对象之后
        @Override
        public int compareTo(@NonNull PriorityRunnable priorityRunnable) {
            int priorityMe = priority;
            int priorityOther = priorityRunnable.getPriority();
            if (priorityMe == priorityOther) {
                return 0;
            } else if (priorityMe > priorityOther) {
                return -1;
            } else {
                return 1;
            }
        }

        @Override
        public void run() {
            useCase.run();
        }

    }// end PriorityRunnable class

    private static class UseCaseFuture extends FutureTask<Object> {
        UseCase useCase;
        UseCaseThreadPoolScheduler scheduler;

        public UseCaseFuture(Runnable runnable, UseCase useCase, UseCaseThreadPoolScheduler scheduler) {
            super(runnable, null);
            this.useCase = useCase;
            this.scheduler = scheduler;
        }

        @Override
        protected void done() {
            //当任务执行完毕后，需要把任务从mFutureMap中移除，防止内存泄露
            if (!useCase.isCacnel()) {
                if (null != useCase.getTag()) {
                    scheduler.mFutureMap.remove(useCase.getTag());
                }
            }
        }
    }// end UseCaseFuture class

    private static class MyThreadPoolExecutor extends ThreadPoolExecutor {

        public MyThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                    BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        }

        @Override
        public Future<?> submit(Runnable task) {
            PriorityRunnable priorityRunnable = (PriorityRunnable) task;
            UseCaseFuture ftask = new UseCaseFuture(task, priorityRunnable.useCase, priorityRunnable.scheduler);
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

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == ADD) {
                PriorityRunnable runnable = (PriorityRunnable) msg.obj;
                scheduler.performSubmit(runnable, runnable.useCase.getTag());
            } else if (what == CANCEL) {
                scheduler.performCancel(msg.obj);
            } else if (what == POST_RESULT) {
                Runnable runnable = (Runnable) msg.obj;
                runnable.run();
            } else if (what == POST_ERROR) {
                Runnable runnable = (Runnable) msg.obj;
                runnable.run();
            }
        }
    }// end InternalHandler

}
