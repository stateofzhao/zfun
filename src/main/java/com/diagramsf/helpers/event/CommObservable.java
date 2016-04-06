package com.diagramsf.helpers.event;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.Serializable;
import java.lang.ref.WeakReference;


/**
 * 只包含一个回调方法的简单被订阅者.
 * */
public class CommObservable extends Observable<CommObserver> {

	private static final int MESSAGE_WHAT = 1;

	private final Handler mUIHandler;// 主线程handler

	private static CommObservable singleton;

	private CommObservable() {
		mUIHandler = new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case MESSAGE_WHAT:
					notifyDo((Serializable)msg.obj);
					break;
				default:
					break;
				}

			}

		};
	}

	public static CommObservable instance() {
		// 双重检测机制
		if (null == singleton) {
			synchronized (UserStateObservable.class) {
				if (null == singleton) {
					singleton = new CommObservable();
				}
			}
		}
		return singleton;
	}

	/**
	 * 通知所有订阅者，用户登录成功
	 * */
	public void doSomething(Serializable obj) {
		if (isMain()) {
			notifyDo(obj);
		} else {
			Message msg = Message.obtain(mUIHandler, MESSAGE_WHAT);
			msg.obj = obj;
			msg.sendToTarget();
		}

	}



	private void notifyDo(Serializable obj) {
		synchronized (mObservers) {
			int size = mObservers.size();
			// since loginSucceed() is implemented by the app, it could do
			// anything, including
			// removing itself from {@link mObservers} - and that could cause
			// problems if
			// an iterator is used on the ArrayList {@link mObservers}.
			// to avoid such problems, just march thru the list in the reverse
			// order.
			for (int i = size - 1; i >= 0; i--) {
				WeakReference<CommObserver> ref = mObservers.get(i);
				CommObserver observer = ref.get();
				if (null != observer) {
					//noinspection unchecked
					observer.doSomething(obj);
				}
			}
		}
	}

	static void checkMain() {
		if (!isMain()) {
			throw new IllegalStateException(
					"Method call should happen from the main thread.");
		}
	}

	static boolean isMain() {
		return Looper.getMainLooper().getThread() == Thread.currentThread();
	}

}
