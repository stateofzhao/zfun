package com.zfun.lib.widget;

import android.content.Context;
import android.os.Parcelable;
import android.os.Process;
import androidx.core.view.MotionEventCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.core.view.ViewConfigurationCompat;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.*;

/**
 * 无限循环的ViewPager
 * <p/>
 * 实现原理就是，把原始数据再复制一份并添加到数据源中。所以第一项的所以position是 原始数据元素个数/2
 * <p/>
 *
 * @author zfun
 */
public class LoopViewPager extends ViewPager {
  private int mAutoScrollTimeInterval = 5000;
  private LoopThread mLoopThread;

  private OnPageChangeListener mOutOnPagerChangeListener;
  private LoopPageAdapterWrapper mAdapterWrapper;

  public LoopViewPager(Context context) {
    super(context);
    init();
  }

  public LoopViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  /**
   * 将循环Pager的索引转换成真正的 数据源索引
   *
   * @param position 循环pager的索引
   */
  public int setRealCurrentItem(int position) {
    if (null == mAdapterWrapper) {
      throw new RuntimeException("需要首先设置适配器！");
    }
    return mAdapterWrapper.setRealCurrentItem(position);
  }

  /** 获取 原始数据的size */
  public int getRealCount() {
    if (null == mAdapterWrapper) {
      throw new RuntimeException("需要首先设置适配器！");
    }
    return mAdapterWrapper.getRealCount();
  }

  /** 获取循环pager的数据大小 */
  public int getLoopViewPagerCount() {
    if (null == mAdapterWrapper) {
      throw new RuntimeException("需要首先设置适配器！");
    }
    return mAdapterWrapper.getCount();
  }

  /**
   * 设置自动滚动的时间 间隔
   *
   * @param mills 单位是毫秒,默认值是 5000
   */
  public void setAutoScrollTimeInterval(int mills) {
    mAutoScrollTimeInterval = mills;
  }

  /** 开启自动循环 */
  public void resumeLoop() {
    initXunHuanThread();
    synchronized (mLoopThread.pauseWorkLock) {
      mLoopThread.isScrollViewPager = true;
      mLoopThread.pauseWorkLock.notifyAll();
    }
  }

  /** 暂停循环焦点图 */
  public void pauseLoop() {
    if (null != mLoopThread) {
      mLoopThread.isScrollViewPager = false;
    }
  }

  /** 循环焦点图的线程是否暂停 */
  public boolean isLoopPaused() {
    return null == mLoopThread || !mLoopThread.isScrollViewPager;
  }

  /** 销毁 循环线程 */
  public void destroyThread() {
    if (null != mLoopThread) {
      if (null != mLoopThread.topLoopTask) {
        removeCallbacks(mLoopThread.topLoopTask);
      }
      mLoopThread.isRun = false;
      mLoopThread.interrupt();
      mLoopThread = null;
    }
  }

  // 初始化 焦点图自动循环的 线程
  private void initXunHuanThread() {
    if (null == mLoopThread) {
      mLoopThread = new LoopThread();
      mLoopThread.start();
    }
  }

  @Override public void setAdapter(PagerAdapter arg0) {
    mAdapterWrapper = new LoopPageAdapterWrapper(arg0);
    super.setAdapter(mAdapterWrapper);
  }

  @Override public void setOnPageChangeListener(OnPageChangeListener listener) {
    this.mOutOnPagerChangeListener = listener;
  }

  @Override public void setCurrentItem(int item, boolean smoothScroll) {
    final int count = getLoopViewPagerCount();
    final int reallyCount = getRealCount();
    if (smoothScroll) {// 这个会触发滚动所以，不用在这里监听
      int looperCurrent = item;
      if (item >= reallyCount) {// 证明要设置的项已经是 在 附加的多余项里了，不需要进行操作

      } else {
        int currentItem = getCurrentItem();
        if (currentItem >= count) {// 证明现在 在附加的多余项里
          looperCurrent = count + item;
        }
      }
      super.setCurrentItem(looperCurrent, true);
    } else {
      int looperCurrent = item;
      if (item == count - 1) {
        looperCurrent = reallyCount - 1;
      } else if (item == 0) {
        looperCurrent = reallyCount;
      }
      super.setCurrentItem(looperCurrent, false);
    }
  }

  @Override public void setCurrentItem(int item) {
    final int count = getLoopViewPagerCount();
    final int reallyCount = getRealCount();

    int looperCurrent = item;
    if (item == count - 1) {
      looperCurrent = reallyCount - 1;
    } else if (item == 0) {
      looperCurrent = reallyCount;
    }
    super.setCurrentItem(looperCurrent);
  }

  private void init() {
    super.setOnPageChangeListener(new OnPageChangeListener() {
      private int focusedPage = 0;

      @Override public void onPageScrollStateChanged(int state) {
        if (null != mOutOnPagerChangeListener) {
          mOutOnPagerChangeListener.onPageScrollStateChanged(state);
        }
        if (state == ViewPager.SCROLL_STATE_IDLE) {
          final int count = getLoopViewPagerCount();
          final int reallyCount = getRealCount();
          if (count == 1) {// 证明原始数据只有一项，那么是不可以无限循环的
            return;
          }
          if (focusedPage == count - 1) {
            LoopViewPager.super.setCurrentItem(reallyCount - 1, false);
          } else if (focusedPage == 0) {
            LoopViewPager.super.setCurrentItem(reallyCount, false);
          }

          //
          // Log.d("tt", "是否正在touch：" + isTouched);
          // Log.d("tt", "是否正在正在向右滑动isSwipeRight：" + isSwipeRight);
          //
          // if (!isTouched) {
          // // 切换到最后一页后，马上切换到第一项
          // if (focusedPage == count - 1) {
          // setCurrentItem(0, false);
          // }
          // } else {
          // if (isSwipeRight) {
          // if (focusedPage == 0) {
          // setCurrentItem(count - 1, false);
          // }
          // } else {
          // if (focusedPage == count - 1) {
          // setCurrentItem(0, false);
          // }
          // }
          //
          // }
          //
          // isSwipeRight = false;
          // isTouched = false;
        }
      }

      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (null != mOutOnPagerChangeListener) {
          mOutOnPagerChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
      }

      @Override public void onPageSelected(int position) {
        focusedPage = position;
        if (null != mOutOnPagerChangeListener) {
          mOutOnPagerChangeListener.onPageSelected(position);
        }
      }
    });
  }

  /** 返回当前item的position，需要用{@link #setRealCurrentItem(int)} 来转换成真正的position */
  @Override public int getCurrentItem() {
    return super.getCurrentItem();
  }

  private float mLastPointX;
  private float mLastPointY;

  private int mTouchSlop;

  private int mActivePointerId = -1;

  private boolean isTouched = false;
  private boolean isSwipeRight = false;

  // 当它的父View拦截掉事件时，不会立即执行父View的onTuchEvent()方法而是会在此View中分发一个ACTION_CANCEL事件(会执行这个方法)传递给此View的子View
  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (0 == mTouchSlop) {
      ViewConfiguration configration = ViewConfiguration.get(getContext());
      mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configration);
    }

    int action = ev.getAction() & MotionEvent.ACTION_MASK;

    switch (action) {
      case MotionEvent.ACTION_UP:// 如果ViewPager拦截事件成功的话，就不再执行onInterceptTouchEvent()方法了，所以这个状态就不执行了
        isTouched = false;
        isSwipeRight = false;
        break;
      case MotionEvent.ACTION_CANCEL:// 这个方法只有上层view拦截了手势事件后才会调用,所以仍然需要在
        // onTouchEvent()方法中处理
        isTouched = false;
        isSwipeRight = false;
        break;

      case MotionEvent.ACTION_DOWN:
        mLastPointX = ev.getX();
        mLastPointY = ev.getY();
        mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
        break;
      case MotionEvent.ACTION_MOVE:
        final int activePointerId = mActivePointerId;
        if (activePointerId == -1) {
          break;
        }

        final float x = MotionEventCompat.getX(ev, mActivePointerId);
        final float y = MotionEventCompat.getY(ev, mActivePointerId);

        final float dx = x - mLastPointX;
        final float xDiff = Math.abs(dx);
        final float dy = y - mLastPointY;
        final float yDiff = Math.abs(dy);

        if (xDiff < yDiff) {
          ViewParent parent = getParent();
          if (null != parent) {
            //					parent.requestDisallowInterceptTouchEvent(false);
          }
        } else {
          isTouched = true;
          if (dx > 0) {
            isSwipeRight = true;
          } else {
            isSwipeRight = false;
          }
          ViewParent parent = getParent();
          if (null != parent) {
            parent.requestDisallowInterceptTouchEvent(true);
          }
        }
        mLastPointX = x;
        mLastPointY = y;
        break;
    }

    return super.onInterceptTouchEvent(ev);
  }

  @Override public boolean onTouchEvent(MotionEvent ev) {
    // ACTION_DOWN的是 屏幕的边缘，不做处理
    if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
      // Don't handle edge touches immediately -- they may actually belong
      // to one of our
      // descendants.
      return super.onTouchEvent(ev);
    }

    if (getAdapter() == null || getAdapter().getCount() == 0) {
      // Nothing to present or scroll; nothing to touch.
      return super.onTouchEvent(ev);
    }

    int action = ev.getAction() & MotionEvent.ACTION_MASK;
    switch (action) {
      case MotionEvent.ACTION_UP:
        isTouched = false;
        isSwipeRight = false;
        break;
      case MotionEvent.ACTION_CANCEL:
        isTouched = false;
        isSwipeRight = false;
        break;

      case MotionEvent.ACTION_DOWN:
        mLastPointX = ev.getX();
        mLastPointY = ev.getY();
        mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
        break;
      case MotionEvent.ACTION_MOVE:
        final int activePointerId = mActivePointerId;
        if (activePointerId == -1) {
          break;
        }

        final float x = MotionEventCompat.getX(ev, mActivePointerId);
        final float y = MotionEventCompat.getY(ev, mActivePointerId);

        final float dx = x - mLastPointX;
        // final float xDiff = Math.abs(dx);
        final float dy = y - mLastPointY;
        // final float yDiff = Math.abs(dy);

        isTouched = true;
        if (dx > 0) {
          isSwipeRight = true;
        } else {
          isSwipeRight = false;
        }
        ViewParent parent = getParent();
        if (null != parent) {
          parent.requestDisallowInterceptTouchEvent(true);
        }
        mLastPointX = x;
        mLastPointY = y;
        break;
    }

    return super.onTouchEvent(ev);
  }

  /** LoopViewPager的适配器改造器 */
  private class LoopPageAdapterWrapper extends PagerAdapter {
    private PagerAdapter mReallyAdapter;
    private final boolean mIsReallyAdapterWell;
    private final int mReallyAdapterCount;
    private final int mMyAdapterCount;

    public LoopPageAdapterWrapper(PagerAdapter adapter) {
      this.mReallyAdapter = adapter;
      if (mReallyAdapter != null) {
        mIsReallyAdapterWell = true;
      } else {
        mIsReallyAdapterWell = false;
      }
      if (!mIsReallyAdapterWell) {
        mReallyAdapterCount = 0;
        mMyAdapterCount = 0;
        return;
      }

      mReallyAdapterCount = adapter.getCount();

      if (mReallyAdapterCount == 1 || mReallyAdapterCount == 0) {// 原始适配器中没有项或者只有1项时，不能进行循环
        mMyAdapterCount = mReallyAdapterCount;
      } else {
        mMyAdapterCount = mReallyAdapterCount * 2;// 让修改后适配器的大小是原先的2倍，这样正好能够无限循环
      }
    }

    public int getRealCount() {
      return mReallyAdapterCount;
    }

    public int setRealCurrentItem(int position) {
      return position % mReallyAdapterCount;
    }

    // 这里有一个问题就是 参数不能够是 List<Object> orangeData,具体为什么解释如下：
    // Illegal code - because otherwise life would be Bad
    // List<Dog> dogs = new List<Dog>();
    // List<Animal> animals = dogs; // Awooga awooga
    // animals.add(new Cat());
    // Dog dog = dogs.get(0); // This should be safe, right?

    @Override public int getCount() {
      return mMyAdapterCount;
    }

    @Override public boolean isViewFromObject(View arg0, Object arg1) {
      return mReallyAdapter.isViewFromObject(arg0, arg1);
    }

    @Override public void destroyItem(View container, int position, Object object) {
      mReallyAdapter.destroyItem(container, position, object);
    }

    @Override public void destroyItem(ViewGroup container, int position, Object object) {
      mReallyAdapter.destroyItem(container, position, object);
    }

    @Override public void finishUpdate(View container) {
      mReallyAdapter.finishUpdate(container);
    }

    @Override public void finishUpdate(ViewGroup container) {
      mReallyAdapter.finishUpdate(container);
    }

    @Override public int getItemPosition(Object object) {
      return mReallyAdapter.getItemPosition(object);
    }

    @Override public CharSequence getPageTitle(int position) {
      return mReallyAdapter.getPageTitle(position);
    }

    @Override public float getPageWidth(int position) {
      return mReallyAdapter.getPageWidth(position);
    }

    @Override public Object instantiateItem(View container, int position) {
      // final int reallyPosition = setRealCurrentItem(position);
      // //不能在这里转换，必须让使用者在 自己的adapter中手动转换来取数据

      return mReallyAdapter.instantiateItem(container, position);
    }

    @Override public Object instantiateItem(ViewGroup container, int position) {
      // final int reallyPosition = setRealCurrentItem(position);
      return mReallyAdapter.instantiateItem(container, position);
    }

    @Override public void notifyDataSetChanged() {
      mReallyAdapter.notifyDataSetChanged();
    }

    @Override public void restoreState(Parcelable state, ClassLoader loader) {
      mReallyAdapter.restoreState(state, loader);
    }

    @Override public Parcelable saveState() {
      return mReallyAdapter.saveState();
    }

    @Override public void setPrimaryItem(View container, int position, Object object) {
      mReallyAdapter.setPrimaryItem(container, position, object);
    }

    @Override public void setPrimaryItem(ViewGroup container, int position, Object object) {
      mReallyAdapter.setPrimaryItem(container, position, object);
    }

    @Override public void startUpdate(View container) {
      mReallyAdapter.startUpdate(container);
    }

    @Override public void startUpdate(ViewGroup container) {
      mReallyAdapter.startUpdate(container);
    }
  }//LoopPageAdapterWrapper end

  /**
   * 自动更改pager position的线程
   */
  class LoopThread extends Thread {
    private boolean isScrollViewPager = false;
    public boolean isRun = true;

    private final Object pauseWorkLock = new Object();

    private ChangeTopPagerPositionRunnable topLoopTask;

    public void run() {
      Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
      try {
        while (isRun) {
          sleep(mAutoScrollTimeInterval);
          synchronized (pauseWorkLock) {
            if (!isScrollViewPager) {
              pauseWorkLock.wait();
            }
            topLoopTask = new ChangeTopPagerPositionRunnable();
            post(topLoopTask);// 执行headerView上的ViewPager循环
          }
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }// class end

  /**
   * 更改顶部Pager position的任务
   */
  class ChangeTopPagerPositionRunnable implements Runnable {
    @Override public void run() {
      setCurrentItem(getCurrentItem() + 1, true);
    }
  }// class end
}
