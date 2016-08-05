package com.diagramsf.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * 支持height 设置为 wrap_content 的ViewPager
 * <p/>
 * Created by Diagrams on 2016/3/23 14:59
 */
public class WrapContentHeightViewPager extends ViewPager {

  /**
   * Constructor
   *
   * @param context the context
   */
  public WrapContentHeightViewPager(Context context) {
    super(context);
  }

  /**
   * Constructor
   *
   * @param context the context
   * @param attrs the attribute set
   */
  public WrapContentHeightViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int height = 0;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
      int h = child.getMeasuredHeight();
      if (h > height) {
        height = h;
      }
    }
    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}
