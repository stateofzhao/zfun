package com.diagramsf.learn.executor;

import android.os.Looper;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class Util {
    public final static String THREAD_NAME = "MyExecutor-Task";

	/**
	 * A handler for rejected tasks that throws a
	 * {@code RejectedExecutionException}.
	 */
	public static class AbortPolicy implements RejectedExecutionHandler {
		/**
		 * Creates an {@code AbortPolicy}.
		 */
		public AbortPolicy() {
		}

		/**
		 * Always throws RejectedExecutionException.
		 *
		 * @param r
		 *            the runnable task requested to be executed
		 * @param e
		 *            the executor attempting to execute this task
		 * @throws RejectedExecutionException
		 *             always
		 */
		public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
			throw new RejectedExecutionException("Task " + r.toString()
					+ " rejected from " + e.toString());
		}
	}

	// 线程池的线程工厂
	public static class MyThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			return new MyThread(r);
		}
	}//class end

	private static class MyThread extends Thread {

		public MyThread(Runnable r) {
			super(r);
		}

		@Override
		public void run() {
			setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			super.run();
		}

	}//class end

    public static void checkMain() {
        if (!isMain()) {
            throw new IllegalStateException(
                    "Method call should happen from the main thread.");
        }
    }

    public static boolean isMain() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

}//class end
