package com.diagramsf.learn;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 * Activity的基类 继承自 {@link FragmentActivity}
 * <p>
 * 需要注意：
 * <p>
 * 1.由于调用{@link FragmentTransaction#commit()}这个方法会立即改变Activity的 保存状态，所以必须在
 * {@link #onSaveInstanceState(Bundle)} 之前调用，否则会崩溃。另外由于 3.0 以前的版本 和 3.0 及以后版本的
 * Activity生命周期的差异 ，为了用户更好的体验，{@link FragmentTransaction#commit()} 都可以在
 * {@link #onStop()} 之前调用 ，只是 在 3.0 以前的版本如果在 {@link #onPause()}和
 * {@link #onStop()} 之间调用会造成 Fragment的状态丢失（在Activity中的视图层次结构状态）不会造成崩溃。如果 在
 * {@link #onStop()} 之后调用 3.0 之前和之后的版本都会 崩溃。 <br>
 * 
 * 2. {@link FragmentTransaction#commit()} 最好放在 {@link #onCreate(Bundle)} 或者
 * 用户操作事件（例如点击事件）中 如果有需要也可以放到 FragmentActivity#onResumeFragments() or
 * Activity#onPostResume() 方法中(这两个方法会确保在
 * Activity完全恢复到原始状态后调用)。不要放到Activity的其它生命周期方法中例如 onActivityResult(), onStart(),
 * 和 onResume(),例如 放到onResume()方法中，在某些情况下，Activity还没有从保存的状态中恢复过来就会调用
 * onResume()方法，此时 就会造成 commit() 提交的Fragment改变 Activity保存的状态。 <br>
 * 
 * 3.避免在异步任务回调总 提交Fragment事务，如果实在没办法，最后只能选择
 * {@link FragmentTransaction#commitAllowingStateLoss()}方法。 <br>
 * 
 * 4.注明 “不属于activity的生命周期” 等方法，不会一直随着activity声明周期变化而执行。例如
 * {@link #onSaveInstanceState(Bundle)},当 activity A跳转到 activity
 * B,然后用户返回activity A时 activity B的 {@link #onPause()}和 {@link #onStop()}会调用，而
 * {@link #onSaveInstanceState(Bundle)}不会调用,因为 android 没有必要调用activity B的
 * onSaveInstanceState()来保存activity B的状态以便恢复activity B<br/>
 * 详细见SDK：{@value http://developer.android.com/reference/android/app/Activity.html
 * #onSaveInstanceState(android.os.Bundle) }
 * */
public class LearnActivity extends FragmentActivity {

	/** 生命周期开始 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	/**
	 * 不属于activity的生命周期
	 * <P>
	 * 
	 * 此方法只有当 activity从以前保存的状态重新初始化时调用。一般大多数恢复操作在 {@link #onCreate(Bundle)}
	 * 中实现即可， 但是对于一些需要在 {@link #onStart()}之后操作的可以放到这里
	 * <P>
	 * android系统会在这个方法中恢复 系统保存的View状态，例如View的焦点信息等
	 * */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);// 必须用，除非你自己完全实现View状态的保存
	}

	/**
	 * 
	 * 一般不需要实现，android系统来做最后的初始化
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/** 分发 onResume() 给fragments */
	@Override
	protected void onPostResume() {
		super.onPostResume();
	}

	// ==只存在于FragmentActivity中
	/** Fragment 执行完了 它们的onResume()方法 */
	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
	}

	/**
	 * 
	 * 不属于activity的生命周期
	 * <P>
	 * 
	 * 在android 3.0 以前（不包含3.0），当系统自动回收Activity时，会在 {@link #onPause()} 之前调用（在
	 * {@link #onResumeFragments()} 之后调用）；在 3.0 及以后版本中 当系统自动回收Activity时，会在
	 * {@link #onStop()} 之前调用（在 {@link #onPause()} 之后）。
	 * <P>
	 * 这个方法和 {@link #onRetainNonConfigurationInstance()}有什么不同，分别应该什么时候调用？see
	 * onRetainNonConfigurationInstance() 注释<Br>
	 * 
	 * 
	 * */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);// 必须调用，因为android系统会自动保存一些View堆栈信息，例如哪个View当前获取焦点了等
	}

	/**
	 * 在android 3.0 以前（不包括3.0），系统自动回收Activity时（android系统内存过低），会在此方法执行完之后才回收
	 * Activity；在 3.0 及以后版本中 会在 {@link #onStop()} 执行完后才回收 Activity。
	 */
	@Override
	protected void onPause() {
		super.onPause();
	}
	

	@Override
	protected void onStop() {
		super.onStop();
	}

	// ==这个方法存在于Activity中，但是在FragmentActivity中被重写成 final
	// 方法了所以不能再重写了,
	// 但是可以使用非UI fragment来替代这个方法能够实现的功能.
	/**
	 * 不属于activity的生命周期
	 * <P>
	 * 
	 * ================================================
	 * 1.这个方法会在 一个activity 执行onDestroy()后立刻又创建一个新的此 activity 的情况下执行 ，典型的案例就是
	 * Configuration改变时。 <br>
	 * 
	 * 2.这个方法返回的 Object，重新生成的activity的 getLastNonConfigurationInstance() 方法 返回。
	 * ================================================
	 * 
	 * <P>
	 * ================================================
	 * 此方法和 {@link #onSaveInstanceState(Bundle)} 不同， <br>
	 * 
	 * ● {@link #onSaveInstanceState(Bundle)}总是会调用（ Configuration
	 * 改变、android回收activity、android 结束application进程--这也是为什么是Bundle保存数据因为
	 * Bundle可以转换成byte[]来进行进程间数据传递、正常的activity切换等 ）；<br>
	 * 
	 * ●{@link #onSaveInstanceState(Bundle)}允许把序列化数据保存到 android系统的 存储区域，这些数据可以
	 * 在以后使用，即使application processes被杀死，而 此方法是把数据 暂时保存到 application中，另外destroyed
	 * activity可能不会被回收。
	 * ================================================
	 * <P>
	 * 
	 * ================================================
	 * 另外，当activity销毁并立即创建时，android系统是禁用 主线程的消息队列的，也就是如果 Object中保留了一个 AyncTask
	 * ,在重建activity的过程中是不会调用 onPostExectue(Result),直到 activity 的onCreate(Bundle)方法执行完。
	 * */
//	 @Override
//	 public Object onRetainNonConfigurationInstance() {
//	 return super.onRetainNonConfigurationInstance();
//	 }

	// ==这个方法存在于FragmentActivity中，不存在于Activity中
	/**
	 * 不属于activity的生命周期
	 * <P>
	 * 
	 * 和 activity中的 onRetainNonConfigurationInstance()用法一样,获取此方法返回的Object使用
	 * {@link #getLastCustomNonConfigurationInstance()}
	 * */
	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		return super.onRetainCustomNonConfigurationInstance();
	}

	/**
	 * 并不会保证被调用。
	 * 
	 * */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 不属于activity的生命周期
	 * <P>
	 * 如果Configuration 改变时，android会destory activity，然后重建activity,不会回调此方法。<Br>
	 * 但是 如果在 某些 Configuration 改变时你不想让android重启你的activity，可以在
	 * android:configChanges 中指定你需要自己接管Configuration的哪些改变。
	 * 当指定的Configuration改变时，android不会重启你的activity，而是会回调此方法。
	 * <P>
	 * */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}
