package com.diagramsf.core.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * 应用程序Activity管理类：用于Activity管理和应用程序退出。
 * <p>
 * 注意：必须在主线程中调用该类的方法。
 * <p>
 * LZF 修改：对activity的引用改为 弱引用。
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.2 添加Activity被回收时的回调接口
 * @created 2012-3-21
 */
public class AppManager {

  private static AppManager instance;

  // 以弱引用的方式存储activity，这里有一个问题就是 当 弱引用指向的activity被回收后，弱引用仍然会存在于Stack（强引用）队列中，
  // 为了清除无用的弱引用（避免过多的弱引用WeakReference
  // 对象造成OOM），需要使用ReferenceQueue来清除无用的WeakReference。
  private Stack<ActivityRef> activityStack;

  // 1.用来清除无用的WeakReference对象用的。
  // 2.ReferenceQueue中存放的是对应的引用（例如，WeakReference）；
  // 这里需要注意它的泛型<Activity>,并不是说ReferenceQueue中存放的是Activity，
  // 而是说ReferenceQueue中存放的WeakReference弱引用的泛型是Activity。
  // 3.不需要手动清除它的元素，系统会自己维护。
  private ReferenceQueue<Activity> q;

  /**
   * 当Activity被虚拟机回收时（在{@link AppManager}中表示被{@link WeakReference}引用的Activity被回收时），
   * 会回调此接口中的{@link #activityFinalize()}方法。
   */
  public interface ActivityFinalize {
    /**
     * 当Activity被虚拟机回收时（在{@link AppManager}中表示被{@link WeakReference}引用的Activity被回收时），
     * 会回调此方法。
     */
    void activityFinalize();
  }

  private AppManager() {
    q = new ReferenceQueue<>();
    activityStack = new Stack<>();
  }

  // 清除那些所弱引用的Activity对象已经被回收的ActivityRef对象
  private void cleanReferenceQueue() {
    ActivityRef ref;
    while ((ref = (ActivityRef) q.poll()) != null) {//如果q中有元素
      activityStack.remove(ref);
      if (ref.activity instanceof ActivityFinalize) {
        ((ActivityFinalize) ref.activity).activityFinalize();
      }
      ref.activity = null;
    }
  }

  /**
   * 单一实例
   */
  public static AppManager getAppManager() {
    if (instance == null) {
      instance = new AppManager();
    }
    return instance;
  }

  /** 从{@link #activityStack} 获取对应的 元素 */
  private ActivityRef getRefFromActivityStack(Activity activity) {
    for (ActivityRef ref : activityStack) {
      Activity act = ref.get();
      if (act == activity) {
        return ref;
      }
    }
    return null;
  }

  /**
   * 添加Activity到堆栈
   */
  public void addActivity(Activity activity) {
    activityStack.add(new ActivityRef(activity, q));
  }

  /** 把Activity从堆栈中弹出 */
  public void popActivity(Activity activity) {
    if (null != activity) {

      ActivityRef ref = getRefFromActivityStack(activity);
      if (null != ref) {
        activityStack.remove(ref);
      }
    }
    cleanReferenceQueue();
  }

  /**
   * 获取当前Activity（堆栈中最后一个压入的）
   */
  public Activity currentActivity() {
    Activity activity = null;
    try {
      activity = activityStack.lastElement().get();
    } catch (NoSuchElementException e) {
      e.printStackTrace();
    }
    return activity;
  }

  /**
   * 结束当前Activity（堆栈中最后一个压入的）
   */
  public void finishActivity() {
    ActivityRef ref = activityStack.lastElement();
    Activity activity = ref.get();
    if (null != activity) {
      activity.finish();
    }
    activityStack.remove(ref);
  }

  /**
   * 结束指定的Activity
   */
  public void finishActivity(Activity activity) {
    if (activity != null) {
      ActivityRef ref = getRefFromActivityStack(activity);
      if (null == ref) {
        return;
      }
      activityStack.remove(ref);
      Activity act = ref.get();
      if (null != act) {
        act.finish();
      }
    }
  }

  /**
   * 结束指定类名的Activity
   */
  public void finishActivity(Class<?> cls) {
    for (int i = activityStack.size() - 1; i >= 0; i--) {
      ActivityRef ref = activityStack.get(i);

      Activity act = ref.get();

      if (null != act && act.getClass().equals(cls)) {
        activityStack.remove(ref);
        act.finish();
        break;
      }
    }
  }

  /**
   * 结束所有Activity
   */
  public void finishAllActivity() {
    for (int i = 0, size = activityStack.size(); i < size; i++) {
      ActivityRef ref = activityStack.get(i);
      if (null != ref) {
        Activity act = ref.get();
        if (null != act) {
          act.finish();
        }
      }
    }
    activityStack.clear();
  }

  /**
   * 获取当前有多少activity处于打开状态
   */
  public int getActivityCountInShow() {
    int total = activityStack.size();
    for (ActivityRef ref : activityStack) {
      Activity act = ref.get();
      if (act == null) {
        total--;
      }
    }

    return total;
  }

  /**
   * 获取指定类名的 activity
   * <p>
   * 目前觉着这个方法有问题，慎重使用!如果同一个Activity类，被打开多次的话，这里只是会返回最初那个Activity
   */
  public Activity getPointActivityByClass(Class<? extends Activity> cls) {
    for (ActivityRef ref : activityStack) {
      Activity act = ref.get();
      if (null != act && act.getClass().equals(cls)) {
        return act;
      }
    }

    return null;
  }

  /**
   * 退出应用程序
   */
  public void AppExit(Context context) {
    try {
      finishAllActivity();
      ActivityManager activityMgr =
          (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
      activityMgr.killBackgroundProcesses(context.getPackageName());
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 自定义WeakReference。
   * <p>
   * 自定义的目的就是把Activity给强引用起来，然后再配合{@link ReferenceQueue}可以达到监听WeakReference引用的Activity回收时，
   * 执行Activity中的相关方法来释放一些可能导致Activity泄露的资源。
   */
  private static class ActivityRef extends WeakReference<Activity> {
    //这里虽然是强引用了 activity，但是由于此处引用是处于 WeakReference中，所以不会阻碍JVM回收activity的
    public Activity activity;

    // 在这里注意
    // ReferenceQueue的泛型，表示的是ReferenceQueue中存放的无用的WeakReference弱引用的Activity
    public ActivityRef(Activity r, ReferenceQueue<Activity> q) {
      super(r, q);
      activity = r;
    }
  }//class end
}//class end