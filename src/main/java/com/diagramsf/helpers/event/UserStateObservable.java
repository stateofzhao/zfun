package com.diagramsf.helpers.event;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 用户登录和登出 事件 的被订阅者,可以使用 {@link UserStateObservable}来订阅接收事件。 <br>
 * 单例模式，使用 {@link #instance()}获取实例
 * */
public class UserStateObservable extends Observable<UserStateObserver> {

	private static final int MESSAGE_USERLOGIN = 1;
	private static final int MESSAGE_USERLOGINOUT = 2;

	private final Handler mUIHandler;// 主线程handler

	private static UserStateObservable singleton;

	private UserStateObservable() {
		mUIHandler = new Handler(Looper.getMainLooper()) {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case MESSAGE_USERLOGIN:
					notifyLoginSucceed();
					break;
				case MESSAGE_USERLOGINOUT:
					notifyLoginOut();
					break;
				default:
					break;
				}

			}

		};
	}

	public static UserStateObservable instance() {
		// 双重检测机制
		if (null == singleton) {
			synchronized (UserStateObservable.class) {
				if (null == singleton) {
					singleton = new UserStateObservable();
				}
			}
		}
		return singleton;
	}

	/**
	 * 通知所有订阅者，用户登录成功
	 * */
	public void userLoginSucceed() {
		if (isMain()) {
			notifyLoginSucceed();
		} else {
			Message msg = Message.obtain(mUIHandler, MESSAGE_USERLOGIN);
			msg.sendToTarget();
		}

	}

	/**
	 * 通知所有订阅者，用户注销登录
	 * */
	public void userLoginOut() {
		if (isMain()) {
			notifyLoginOut();
		} else {
			Message msg = Message.obtain(mUIHandler, MESSAGE_USERLOGINOUT);
			msg.sendToTarget();
		}

	}

	private void notifyLoginSucceed() {
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
				WeakReference<UserStateObserver> ref = mObservers.get(i);
				UserStateObserver observer = ref.get();
				if (null != observer) {
					observer.userLoginSucceed();
				}
			}
		}
	}

	private void notifyLoginOut() {
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
				WeakReference<UserStateObserver> ref = mObservers.get(i);
				UserStateObserver observer = ref.get();
				if (null != observer) {
					observer.userLoginOut();
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
