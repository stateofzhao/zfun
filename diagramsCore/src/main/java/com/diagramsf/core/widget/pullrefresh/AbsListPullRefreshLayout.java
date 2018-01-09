package com.diagramsf.core.widget.pullrefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;

/**
 * 专门针对AbsListView 优化的下拉刷新 .
 * <p/>
 * 注意：
 * 1. ContentView (也就是第二个ChildView)必须是AbsListView，并且AbsListView的背景必须是透明的.
 * 2. AbsListView 必须不能在外部覆盖设置{@link AbsListView#setOnScrollListener(AbsListView.OnScrollListener)},
 * 如果要设置，也需要自己重写继承自{@link AbsListView}或其子类，来监听。
 */
public class AbsListPullRefreshLayout extends PullRefreshLayout {

  private boolean mContentViewIsAbsListView = false;//ContentView是否是AbsListView
  private boolean mFirstScrollToPosition = true;

  private int mInitPaddingTop = 0;
  private int mInitPaddingBom = 0;
  private int mInitPaddingLeft = 0;
  private int mInitPaddingRight = 0;

  private int mInitFirstViewGetTop = 0;//AbsListView中第一个View距离AbsListView顶部的初始位置

  public AbsListPullRefreshLayout(Context context) {
    super(context);
  }

  public AbsListPullRefreshLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AbsListPullRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public AbsListPullRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void contentViewScrollToPosition(boolean isRefreshToNormal, float yDirection, int x,
      int y) {
    if (isGoodDoAbsListPadding()) {
      if (mFirstScrollToPosition) {
        initData(mContentView);
        mFirstScrollToPosition = false;
      }

      final AbsListView av = (AbsListView) mContentView;
      av.setPadding(mInitPaddingLeft, mInitPaddingTop + Math.abs(y), mInitPaddingRight,
          mInitPaddingBom);
      if (!isRefreshToNormal) {
        av.forceLayout();
        av.setSelection(0);
      }
    } else {
      super.contentViewScrollToPosition(isRefreshToNormal, yDirection, x, y);
    }
  }

  @Override protected int getContentViewOffsetFromTop() {
    if (isGoodDoAbsListPadding()) {
      return -mContentView.getPaddingTop() + mInitPaddingTop;
    } else {
      return super.getContentViewOffsetFromTop();
    }
  }

  @Override protected boolean isContentViewOffsetFromTop() {
    if (isGoodDoAbsListPadding()) {
      return mContentView.getPaddingTop() - mInitPaddingTop > 0;
    } else {
      return super.isContentViewOffsetFromTop();
    }
  }

  @Override protected boolean customContentViewAchieveEvent(float yDirection, State state) {
    if (isGoodDoAbsListPadding() && state == State.REFRESHING && yDirection < 0) {
      final int offsetTop = mContentView.getPaddingTop() - mInitPaddingTop;
      if (offsetTop == getRefreshHeight()) {
        return true;
      }
    }
    return super.customContentViewAchieveEvent(yDirection, state);
  }

  @Override public boolean canChildScrollUp(View view) {
    if (view instanceof AbsListView) {
      final AbsListView absListView = (AbsListView) view;
      return absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0
          || absListView.getChildAt(0).getTop() < absListView.getPaddingTop());
    } else {
      return super.canChildScrollUp(view);
    }
  }

  @Override protected void onInitContentView(View contentView) {
    super.onInitContentView(contentView);
    mFirstScrollToPosition = true;
    if ((contentView instanceof AbsListView)) {
      if (osAboveAPI10()) {
        initData(contentView);

        ((AbsListView) contentView).setClipToPadding(false);
      }
      mContentViewIsAbsListView = true;

      ((AbsListView) mContentView).setOnScrollListener(new AbsListView.OnScrollListener() {
        @Override public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
          if (null == mRefreshHeaderView) {
            return;
          }

          if (mState == State.REFRESHING) {
            if (view.getChildCount() == 0 || view.getChildAt(0).getTop() <= mInitFirstViewGetTop) {
              mRefreshHeaderView.setVisibility(View.INVISIBLE);
            } else {
              mRefreshHeaderView.setVisibility(View.VISIBLE);
            }
          }
        }
      });
    } else {
      mContentViewIsAbsListView = false;
    }
  }

  private boolean osAboveAPI10() {
    return Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1;
  }

  private boolean isGoodDoAbsListPadding() {
    return osAboveAPI10() && mContentViewIsAbsListView;
  }

  private void initData(View contentView) {
    mInitPaddingTop = contentView.getPaddingTop();
    mInitPaddingBom = contentView.getPaddingBottom();
    mInitPaddingLeft = contentView.getPaddingLeft();
    mInitPaddingRight = contentView.getPaddingRight();

    if (null != ((AbsListView) mContentView).getChildAt(0)) {
      mInitFirstViewGetTop = ((AbsListView) mContentView).getChildAt(0).getTop();
    }
  }
}
