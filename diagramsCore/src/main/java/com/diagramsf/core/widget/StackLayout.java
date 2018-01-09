package com.diagramsf.core.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 橫向单行排队Layout，第一个View不会把后面的View挤出屏幕，后面的View就紧随第一个View。
 * <P>
 * Created by Diagrams on 2016/11/12 16:31
 */
public class StackLayout extends ViewGroup {
  public StackLayout(Context context) {
    super(context);
  }

  public StackLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public StackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public StackLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  //这里的测量思路是，首先根据参数withMeasureSpec和heightMeasureSpec来让子View自己来测量自己的尺寸，
  // 然后再根据子View的measureWidth来确定是否重新给子View设置measureWidth这样就达到了，自己控制子View的尺寸的目的。
  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int maxWidth = 0;
    int maxHeight = 0;

    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);

    int childrenCount = getChildCount();
    int childrenWidth = 0;
    int childrenHeight = 0;
    for (int c = 0; c < childrenCount; c++) {
      View childView = getChildAt(c);
      //挨个测量子View尺寸，这样后面就可以根据子View的getMeasureWidth()来计算是否需要重新设置子View尺寸
      measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);

      LayoutParams params = (LayoutParams) childView.getLayoutParams();
      childrenWidth += childView.getMeasuredWidth();
      childrenWidth += params.leftMargin + params.rightMargin;

      //去子View的最大高度
      if (childrenHeight < childView.getMeasuredHeight()) {
        childrenHeight = childView.getMeasuredHeight();
        childrenHeight += params.topMargin + params.bottomMargin;
      }
    }
    if (widthMode == MeasureSpec.UNSPECIFIED) {//父View询问子View的尺寸，就是子View可以设置自己任意尺寸，这个一般作为列表Item时出现
      maxWidth = childrenWidth;
    } else if (widthMode == MeasureSpec.EXACTLY) {//父View明确告诉了子View的尺寸
      maxWidth = MeasureSpec.getSize(widthMeasureSpec);
    } else if (widthMode == MeasureSpec.AT_MOST) {//父View给子View指定了一个最大尺寸
      maxWidth = Math.min(MeasureSpec.getSize(widthMeasureSpec), childrenWidth);
    }

    if (heightMode == MeasureSpec.UNSPECIFIED) {
      maxHeight = childrenHeight;
    } else if (heightMode == MeasureSpec.EXACTLY) {
      maxHeight = MeasureSpec.getSize(heightMeasureSpec);
    } else if (heightMode == MeasureSpec.AT_MOST) {
      maxHeight = Math.min(MeasureSpec.getSize(heightMeasureSpec), childrenHeight);
    }
    setMeasuredDimension(maxWidth, maxHeight);

    int selfMeasureWidth = getMeasuredWidth();
    int allChildrenMeasureWidth = 0;
    int count = getChildCount();
    for (int i = 0; i < count; i++) {
      View child = getChildAt(i);
      int measureWidth = child.getMeasuredWidth();
      allChildrenMeasureWidth += measureWidth;

      LayoutParams params = (LayoutParams) child.getLayoutParams();
      allChildrenMeasureWidth += params.leftMargin;
      allChildrenMeasureWidth += params.rightMargin;
    }

    if (allChildrenMeasureWidth > selfMeasureWidth) {//此时子View超出了本View的边界，优先保证后加入的子View的显示
      int measuredWidth = 0;
      int widthByUsed = 0;
      boolean firstClipView = false;
      for (int j = count - 1; j >= 0; j--) {
        View child = getChildAt(j);

        measuredWidth += child.getMeasuredWidth();
        LayoutParams params = (LayoutParams) child.getLayoutParams();
        widthByUsed += params.leftMargin;
        widthByUsed += params.rightMargin;

        if (measuredWidth + widthByUsed > selfMeasureWidth) {
          if (!firstClipView) {
            firstClipView = true;
            int freeSpace = selfMeasureWidth - widthByUsed;
            int widthSpec = MeasureSpec.makeMeasureSpec(freeSpace, MeasureSpec.EXACTLY);
            int heightSpec =
                MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY);
            child.measure(widthSpec, heightSpec);
          } else {//只有出现了一次对View的切割，那么剩下的所有View都不需要显示了~，所以宽度直接设置为0
            int widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
            int heightSpec =
                MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY);
            child.measure(widthSpec, heightSpec);
          }
        } else {
          widthByUsed += measuredWidth;
        }
      }
    }
  }

  @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
    int paddingTop = getPaddingTop();
    int paddingLeft = getPaddingLeft();

    int childCount = getChildCount();
    int childLeft = paddingLeft;
    for (int i = 0; i < childCount; i++) {
      View child = getChildAt(i);
      LayoutParams params = (LayoutParams) child.getLayoutParams();
      int LeftMargin = params.leftMargin;
      int left = childLeft + LeftMargin;
      int right = left + child.getMeasuredWidth();

      child.layout(left, paddingTop, right, paddingTop + child.getMeasuredHeight());
      if (child.getVisibility() != GONE) {
        childLeft = right + params.rightMargin;
      }
    }
  }

  // 一下四个方法是给AutoLineLayout的子View定义LayoutParams---------------
  @Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new StackLayout.LayoutParams(getContext(), attrs);
  }

  @Override protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
  }

  @Override protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    return new LayoutParams(p);
  }

  @Override protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof StackLayout.LayoutParams;
  }// --------------------------------------------------------------------

  /**
   * StackLayout LayoutParams
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
  }//class end
}
