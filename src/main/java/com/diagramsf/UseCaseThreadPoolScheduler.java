package com.diagramsf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

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

    private InternalHandler mMainHandler;//主线程Handler

    private ExecutorService mExecutorService;

    private Map<Object, List<Future>> mFutureMap;

    public UseCaseThreadPoolScheduler() {
        mFutureMap = new HashMap<>();
        PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>();
        mExecutorService = new ThreadPoolExecutor(CORE_SIZE, MAX_SIZE, TIMEOUT, TimeUnit.SECONDS, queue);
        mMainHandler = new InternalHandler(this);
    }

    @Override
    public void execute(Runnable task, Object tag) {
        Message msg = Message.obtain(mMainHandler);
        msg.what = ADD;
        msg.obj = new RunnableHolder(task, tag);
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
    public <T extends UseCase.ResponseValue> void notifyResult(T response, UseCase.Listener<T> listener) {
        Message msg = Message.obtain(mMainHandler);
        msg.what = POST_RESULT;
    }

    @Override
    public <E extends UseCase.ErrorValue> void error(E error, UseCase.ErrorListener<E> errorListener) {

    }

    private void performSubmit(Runnable runnable, Object tag) {
        Future future = mExecutorService.submit(runnable);
        if(null != tag) {
            List<Future> futures = mFutureMap.get(tag);
            if (null == futures) {
                futures = new ArrayList<>();
            }
            futures.add(future);
            mFutureMap.put(tag, futures);
        }
    }

    private void performCancel(Object tag) {
        if(null != tag){
            List<Future> futures = mFutureMap.remove(tag);
            if (null != futures) {
                for (Future future : futures) {
                    future.cancel(false);
                }
            }
        }
    }

    private static class PriorityRunnable<T extends UseCase.RequestValue, V extends UseCase.ResponseValue, E extends UseCase.ErrorValue>
            implements Runnable, Comparable<PriorityRunnable> {
        private int priority;
        private UseCase<T, V, E> useCase;

        public PriorityRunnable(UseCase<T, V, E> useCase) {
            this.useCase = useCase;
            this.priority = useCase.getPriority();
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
            useCase.execute(useCase.getRequestValue());
        }

    }// end PriorityRunnable

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
                RunnableHolder holder = (RunnableHolder) msg.obj;
                scheduler.performSubmit(holder.runnable, holder.tag);
            } else if (what == CANCEL) {

            } else if (what == POST_RESULT) {

            }
        }
    }// end InternalHandler

    private static class RunnableHolder implements Runnable {
        public Runnable runnable;
        public Object tag;

        public RunnableHolder(Runnable runnable, Object tag) {
            this.runnable = runnable;
            this.tag = tag;
        }

        @Override
        public void run() {

        }
    }
}
