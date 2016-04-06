package com.diagramsf.helpers.event;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 参照 {@link android.database.Observable}源码
 * <P>
 * 
 * 被订阅者，泛型T表示订阅者 。 不能够直接实例化，这个类只是实现了 添加订阅，取消订阅的方法，具体实现需要子类来实现。
 * */
public abstract class Observable<T> {

	/**
	 * 存放订阅者的集合，订阅者必须只能添加一次，并且不能为null.
	 */
	protected final ArrayList<WeakReference<T>> mObservers = new ArrayList<>();

	private final ReferenceQueue<T> q = new ReferenceQueue<>();

	private void cleanReferenceQueue() {
		synchronized (mObservers) {
			Reference<? extends T> ref;
			while ((ref = q.poll()) != null) {
				mObservers.remove(ref);
			}
		}

	}

	/**
	 * 添加订阅者进来，订阅者必须不能为空，并且没有被添加过!
	 * */
	public final void registerObserver(T observer) {

		if (null == observer) {
			throw new IllegalArgumentException("this observer is null");
		}

		synchronized (mObservers) {
			cleanReferenceQueue();
			if (mObservers.contains(observer)) {
				throw new IllegalArgumentException("observer" + observer
						+ " is already registered");
			}
			mObservers.add(new WeakReference<>(observer, q));
		}
	}

	/**
	 * 取消订阅者的订阅。参数必须不能为空
	 * */
	public final void unRegisterObserver(T observer) {
		if (null == observer) {
			throw new IllegalArgumentException("this observer is null");
		}

		synchronized (mObservers) {

			WeakReference<T> ref = getRefForObserver(observer);

			// if (null == ref) {
			// throw new IllegalArgumentException("observer" + observer
			// + " is not registered");
			// }

			if (null != ref) {
				mObservers.remove(ref);
			}

		}

	}

	public final void unRegisterAll() {
		synchronized (mObservers) {
			mObservers.clear();
		}
	}

	private WeakReference<T> getRefForObserver(T observer) {

		for (WeakReference<T> ref : mObservers) {
			T t = ref.get();
			if (t == observer) {
				return ref;
			}
		}

		return null;

	}
}
