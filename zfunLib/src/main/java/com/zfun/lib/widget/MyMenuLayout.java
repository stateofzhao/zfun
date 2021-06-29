package com.zfun.lib.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * @author LZF
 * @version 1.0.0
 */
public abstract class MyMenuLayout extends ViewGroup {

  private static final float BLUR_ALPHA = 0.6f;// 遮罩View最终的透明度

  /** 菜单位置 */
  public enum ItemLocation {
    Top, Bom, Left, Right
  }

  private ItemLocation mItemLocation;// 菜单出现的位置

  private boolean mBlurViewCanClick = false;// 遮罩View是否可以点击
  private boolean mIsAnimateItem = false;// 是否以动画的方式显示菜单

  private boolean mUpdateMenuView = false;// 是否更新MenuView中的数据

  private ShowMenuTask mShowMenuTask;// 显示菜单的任务

  private AnimatorSet mShowMenuAnimatorSet;
  private AnimatorSet mHideMenuAnimatorSet;

  private OnMenuVisibleListener mOnMenuVisibleListener;

  /** 监听菜单View的显示和隐藏 */
  public interface OnMenuVisibleListener {
    void onMenuShow();

    void onMenuHide();

    /** 取消菜单的显示 */
    void onShowMenuCancel();

    /** 取消菜单的隐藏 */
    void onHideMenuCancel();
  }

  public MyMenuLayout(Context context) {
    super(context);
    init();
    // 不能在构造函数中来初始化MenuView和BlurView，因为这样的话就不能给MenuView设置数据了(因为onCreateMenuView()方法会被调用)
    // addMenuAndBlurView();
  }

  public MyMenuLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
    // addMenuAndBlurView();
  }

  public MyMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
    // addMenuAndBlurView();
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public MyMenuLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(getDefaultSize(0, widthMeasureSpec),
        getDefaultSize(0, heightMeasureSpec));// 设置自身尺寸

    final int childCount = getChildCount();
    // if (childCount != 2) {
    // throw new RuntimeException("Must have two childView");
    // }

    for (int i = 0; i < childCount; i++) {
      final View childView = getChildAt(i);
      final int visible = childView.getVisibility();
      if (visible != View.GONE) {
        measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);
      }
    }
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    final int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View childView = getChildAt(i);
      if (childView.getVisibility() != View.GONE) {
        final int childWidth = childView.getMeasuredWidth();
        final int childHeight = childView.getMeasuredHeight();
        if (0 == i) { // blur View
          childView.layout(l, t, r, b);
        } else {// item View放到屏幕外面
          switch (mItemLocation) {
            case Top:
              childView.layout(l, 0, l + childWidth, childHeight);
              break;
            case Bom:
              childView.layout(l, b - childHeight, l + childWidth, b);
              break;
          }
        }
      }
    }
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (null != mShowMenuTask) {
      removeCallbacks(mShowMenuTask);
    }
    if (null != mHideMenuAnimatorSet && mHideMenuAnimatorSet.isRunning()) {
      mHideMenuAnimatorSet.cancel();
    }
    if (null != mShowMenuAnimatorSet && mShowMenuAnimatorSet.isRunning()) {
      mShowMenuAnimatorSet.end();
    }
  }

  /** 生成遮罩View */
  protected abstract View onCreateBlurView(ViewGroup parent);

  /**
   * 生成菜单View
   * <p>
   * <p>
   * 注意：对MenuView进行的所有操作必须在这个方法中完成（包括对MenuView进行的赋值）！
   */
  protected abstract View onCreateMenuView(ViewGroup parent);

  /** 菜单显示的位置 */
  protected abstract ItemLocation onCreateItemLocation();

  private OnClickListener mBlurClickListener = new OnClickListener() {

    @Override public void onClick(View v) {
      if (!mBlurViewCanClick) {
        return;
      }
      hideMenu(mIsAnimateItem);
    }
  };

  private void init() {
    mItemLocation = onCreateItemLocation();
  }

  // 添加MenuView和BlurView
  private void addMenuAndBlurView() {
    final View blurView = onCreateBlurView(this);
    final View itemView = onCreateMenuView(this);

    final LayoutParams blurParams =
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    final LayoutParams itemParams =
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

    addView(blurView, blurParams);
    addView(itemView, itemParams);

    mBlurViewCanClick = false;
    blurView.setVisibility(View.INVISIBLE);
    blurView.setOnClickListener(mBlurClickListener);

    itemView.setVisibility(View.INVISIBLE);
  }

  /** 设置菜单View显示和隐藏的监听器 */
  public void setOnMenuViewVisibleListener(OnMenuVisibleListener listener) {
    mOnMenuVisibleListener = listener;
  }

  /**
   * 是否更新MenuView中的数据
   *
   * @param update true 更新，false不更新
   */
  public final void updateMenuViewData(boolean update) {
    mUpdateMenuView = update;
  }

  /**
   * 显示菜单
   *
   * @param animat 是否动画显示菜单
   */
  public final void showMenu(boolean animat) {
    final int childViewCount = getChildCount();

    if (childViewCount >= 3 || childViewCount == 1) {
      throw new RuntimeException("MyMenuLayout childCount Must is 2");
    }

    if (childViewCount == 0) {
      // 添加菜单View和遮罩View，并且让这两个View都不可见
      addMenuAndBlurView();
    } else {
      if (childViewCount == 2 && mUpdateMenuView) {
        removeAllViews();
        // 添加菜单View和遮罩View，并且让这两个View都不可见
        addMenuAndBlurView();
        mUpdateMenuView = false;
      }
    }

    setVisibility(View.VISIBLE);
    mIsAnimateItem = animat;

    // 只能这么写，否则的话android来不及计算出MenuView的尺寸就会显示菜单，造成菜单显示不出来
    mShowMenuTask = new ShowMenuTask();
    post(mShowMenuTask);
  }

  /**
   * 隐藏菜单
   *
   * @param animat 是否动画隐藏菜单
   */
  public final void hideMenu(boolean animat) {
    if (!isMenuShow()) {
      return;
    }

    final View blurView = getChildAt(0);
    final View itemView = getChildAt(1);
    final int itemViewHeight = itemView.getHeight();

    if (!animat) {
      removeAllViews();// 移除所有View
      mBlurViewCanClick = false;
      // 当首次显示出菜单后，再让菜单消失后， 在 android
      // 2.2上面如果不让整个布局GONE掉的话，会让它下面的布局不能获取touch事件(不知道原因)，
      setVisibility(View.GONE);
      if (null != mOnMenuVisibleListener) {
        mOnMenuVisibleListener.onMenuHide();
      }
    } else {
      switch (mItemLocation) {
        case Top:
          ObjectAnimator topItemShow =
              ObjectAnimator.ofFloat(itemView, "translationY", 0f, -itemViewHeight);
          ObjectAnimator topBlurShow = ObjectAnimator.ofFloat(blurView, "alpha", BLUR_ALPHA, 0f);

          final AnimatorSet topSet = new AnimatorSet();
          topSet.addListener(new Animator.AnimatorListener() {

            @Override public void onAnimationStart(Animator arg0) {
              mBlurViewCanClick = false;
            }

            @Override public void onAnimationRepeat(Animator arg0) {

            }

            @Override public void onAnimationEnd(Animator arg0) {
              Log.d("MyMenuLayout", "菜单隐藏动画结束,菜单隐藏");
              if (null != mOnMenuVisibleListener) {
                mOnMenuVisibleListener.onMenuHide();
              }

              blurView.setVisibility(View.INVISIBLE);
              mBlurViewCanClick = false;
              setVisibility(View.GONE);

              // --删除菜单和遮罩View
              removeAllViews();
            }

            @Override public void onAnimationCancel(Animator arg0) {

              if (null != mOnMenuVisibleListener) {
                mOnMenuVisibleListener.onHideMenuCancel();
              }

              blurView.setVisibility(View.INVISIBLE);
              mBlurViewCanClick = false;
              setVisibility(View.GONE);

              // --删除菜单和遮罩View
              removeAllViews();
            }
          });
          topSet.playTogether(topItemShow, topBlurShow);
          mHideMenuAnimatorSet = topSet;
          topSet.start();
          break;
        case Bom:
          ObjectAnimator bomItemHide =
              ObjectAnimator.ofFloat(itemView, "translationY", 0f, itemViewHeight);
          ObjectAnimator bomBlurHide = ObjectAnimator.ofFloat(blurView, "alpha", BLUR_ALPHA, 0f);

          final AnimatorSet bomSet = new AnimatorSet();
          bomSet.addListener(new Animator.AnimatorListener() {

            @Override public void onAnimationStart(Animator arg0) {
              mBlurViewCanClick = false;
            }

            @Override public void onAnimationRepeat(Animator arg0) {

            }

            @Override public void onAnimationEnd(Animator arg0) {
              blurView.setVisibility(View.INVISIBLE);
              mBlurViewCanClick = false;
              setVisibility(View.GONE);

              Log.d("MyMenuLayout", "菜单隐藏动画结束,菜单隐藏");
              if (null != mOnMenuVisibleListener) {
                mOnMenuVisibleListener.onMenuHide();
              }

              // --删除菜单和遮罩View
              removeAllViews();
            }

            @Override public void onAnimationCancel(Animator arg0) {
              blurView.setVisibility(View.INVISIBLE);
              mBlurViewCanClick = false;
              setVisibility(View.GONE);

              // --删除菜单和遮罩View
              removeAllViews();

              if (null != mOnMenuVisibleListener) {
                mOnMenuVisibleListener.onHideMenuCancel();
              }
            }
          });
          bomSet.playTogether(bomItemHide, bomBlurHide);
          mHideMenuAnimatorSet = bomSet;
          bomSet.start();
          break;
      }
    }
  }

  /** 菜单是否显示，true是显示，false是不显示 */
  public final boolean isMenuShow() {
    // 证明正在进行隐藏菜单的动画
    if (null != mHideMenuAnimatorSet && mHideMenuAnimatorSet.isRunning()) {
      return false;
    }

    // 证明显示菜单的动画正在进行
    if (null != mShowMenuAnimatorSet && mShowMenuAnimatorSet.isRunning()) {
      return true;
    }

    final View blurView = getChildAt(0);

    return null != blurView && blurView.getVisibility() == View.VISIBLE;
  }

  private class ShowMenuTask implements Runnable {

    @Override public void run() {
      final View blurView = getChildAt(0);
      final View itemView = getChildAt(1);
      final int itemViewHeight = itemView.getHeight();

      if (!mIsAnimateItem) {

        Drawable background = blurView.getBackground();
        background.setAlpha((int) (255 * BLUR_ALPHA));
        blurView.setBackgroundDrawable(background);
        blurView.setVisibility(View.VISIBLE);
        itemView.setVisibility(View.VISIBLE);
        mBlurViewCanClick = true;

        if (null != mOnMenuVisibleListener) {
          mOnMenuVisibleListener.onMenuShow();
        }
      } else {
        switch (mItemLocation) {
          case Top:
            ObjectAnimator topItemShow =
                ObjectAnimator.ofFloat(itemView, "translationY", -itemViewHeight, 0f);
            ObjectAnimator topBlurShow = ObjectAnimator.ofFloat(blurView, "alpha", 0f, BLUR_ALPHA);

            final AnimatorSet topSet = new AnimatorSet();
            topSet.addListener(new Animator.AnimatorListener() {

              @Override public void onAnimationStart(Animator arg0) {
                blurView.setVisibility(View.VISIBLE);
                itemView.setVisibility(View.VISIBLE);
                mBlurViewCanClick = false;
              }

              @Override public void onAnimationRepeat(Animator arg0) {

              }

              @Override public void onAnimationEnd(Animator arg0) {
                mBlurViewCanClick = true;

                if (null != mOnMenuVisibleListener) {
                  mOnMenuVisibleListener.onMenuShow();
                }
              }

              @Override public void onAnimationCancel(Animator arg0) {
                mBlurViewCanClick = false;

                removeAllViews();
                setVisibility(View.GONE);

                if (null != mOnMenuVisibleListener) {
                  mOnMenuVisibleListener.onShowMenuCancel();
                }
              }
            });
            topSet.playTogether(topItemShow, topBlurShow);

            mShowMenuAnimatorSet = topSet;

            topSet.start();
            break;
          case Bom:
            ObjectAnimator bomItemShow =
                ObjectAnimator.ofFloat(itemView, "translationY", itemViewHeight, 0f);
            ObjectAnimator bomBlurShow = ObjectAnimator.ofFloat(blurView, "alpha", 0f, BLUR_ALPHA);

            final AnimatorSet bomSet = new AnimatorSet();
            bomSet.addListener(new Animator.AnimatorListener() {

              @Override public void onAnimationStart(Animator arg0) {
                blurView.setVisibility(View.VISIBLE);
                itemView.setVisibility(View.VISIBLE);
                mBlurViewCanClick = false;
              }

              @Override public void onAnimationRepeat(Animator arg0) {

              }

              @Override public void onAnimationEnd(Animator arg0) {
                mBlurViewCanClick = true;

                if (null != mOnMenuVisibleListener) {
                  mOnMenuVisibleListener.onMenuShow();
                }
              }

              @Override public void onAnimationCancel(Animator arg0) {
                mBlurViewCanClick = false;
                removeAllViews();
                setVisibility(View.GONE);

                if (null != mOnMenuVisibleListener) {
                  mOnMenuVisibleListener.onShowMenuCancel();
                }
              }
            });
            bomSet.playTogether(bomItemShow, bomBlurShow);
            mShowMenuAnimatorSet = bomSet;
            bomSet.start();
            break;
        }
      }
    }
  }

  /**
   * 创建默认的遮罩View
   */
  public final View createDefaultBlurView() {
    View blur = new View(getContext());
    blur.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));
    blur.setBackgroundColor(Color.BLACK);
    return blur;
  }

  // 一下四个方法是给AutoLineLayout的子View定义LayoutParams---------------
  @Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
  }

  @Override protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return new LayoutParams(p);
  }

  @Override protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams;
  }// --------------------------------------------------------------------

  /**
   * MyMenuLayout的 LayoutParams,不是作用于 MyMenuLayout 自己的（不是设置给
   * MyMenuLayout的），而是作用于其子View的(设置给 MyMenuLayout 子View的)！
   */
  public static class LayoutParams extends MarginLayoutParams {

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(ViewGroup.LayoutParams source) {
      super(source);
    }
  }
}
