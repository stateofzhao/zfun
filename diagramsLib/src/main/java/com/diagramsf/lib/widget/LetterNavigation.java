package com.diagramsf.lib.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.diagramsf.lib.R;
import java.util.ArrayList;
import java.util.List;

/**
 * 字母导航条
 * <p/>
 * Created by Diagrams on 2016/6/15 14:43
 *
 * @version 1.0 只支持字符串作为item
 */
public class LetterNavigation extends View {
  private final static String TAG = "LetterNavigation";
  private final static boolean DEBUG = false;

  private final static String REGULAR = "[|]";//在Android中拆分字符串需要加上[]，java中则不需要

  private final static int LETTER_FONT_SIZE = 15;//dp
  private final static int LETTER_SPACE = 10;//dp
  private final static int BG_COLOR = Color.TRANSPARENT;
  private final static int NORMAL_LETTER_COLOR = Color.GRAY;
  private final static int TOUCHED_LETTER_COLOR = Color.BLUE;
  private final static float BG_RX = 20f;
  private final static float BG_RY = 20f;
  private final static boolean SHOW_TOUCHED_BG_COLOR = true;

  private List<Pair<Object, Object>> mLetters;//要显示的字母导航集合
  private int mLetterFontSize = -1;//导航字母字体大小
  private int mLetterSpace = -1;//导航字母间距
  private int mTouchBgColor = -1;//触摸到导航时，显示的背景颜色
  private int mNormalLetterColor = -1;//触摸到导航时，字母色值
  private int mTouchedLetterColor = -1;//没有触摸到导航时，字母色值
  private float mBgRx = 0f; // 背景X轴上的圆角率
  private float mBgRy = 0f; //背景Y轴上的圆角率

  private float mLetterLeftX; //绘制字符的开始X轴坐标
  private float mLetterTopY; //绘制字符的开始Y轴坐标
  private float mLetterRightX;//绘制字符的结束X轴坐标
  private int mChoose;//当前选中的Item的position

  private float[] mItemTopY;//每一项item绘制时的顶部Y坐标
  private float mLastItemBomY;//最后一项item的底部Y坐标

  private Paint mLetterPaint;
  private Paint mTextPaintTemp;

  private BgDrawable mBgDrawable;

  private boolean mShowTouchBg;//是否显示，触摸时的背景
  private boolean mInTouched;//手指是否touch了

  private OnItemSelected mOnItemSelected;

  /** 导航选中通知 */
  public interface OnItemSelected {
    /**
     * @param position item位置索引
     * @param positionItem item显示的值 对应于{@link #setLetters(List)}中Pair的第一个参数
     * @param positionValue item携带的数据，对应于{@link #setLetters(List)}中Pair的第二个参数
     */
    void onSelected(int position, Object positionItem, Object positionValue);
  }

  public LetterNavigation(Context context) {
    super(context);
    parseAttrs(context, null, 0, 0);
    init(context);
  }

  public LetterNavigation(Context context, AttributeSet attrs) {
    super(context, attrs);
    parseAttrs(context, attrs, 0, 0);
    init(context);
  }

  public LetterNavigation(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    parseAttrs(context, attrs, defStyleAttr, 0);
    init(context);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public LetterNavigation(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    parseAttrs(context, attrs, defStyleAttr, defStyleRes);
    init(context);
  }

  public void setLetters(@NonNull List<Pair<Object, Object>> letters) {
    mLetters = letters;
    mItemTopY = new float[letters.size()];
    requestLayout();
  }

  public void showTouchBg(boolean show) {
    mShowTouchBg = show;
    requestLayout();
  }

  public void setOnItemSelected(OnItemSelected listener) {
    mOnItemSelected = listener;
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (mLetters.size() == 0) {
      //如果直接调用super的onMeasure()方法的话，根据getSuggestedMinimumWidth() 和 getSuggestedMinimumHeight()
      //来设置setMeasuredDimension()方法
      setMeasuredDimension(0, 0);
    } else {
      int width = 0;
      int height = 0;

      int widthMode = MeasureSpec.getMode(widthMeasureSpec);
      int heightMode = MeasureSpec.getMode(heightMeasureSpec);
      int widthSize = MeasureSpec.getSize(widthMeasureSpec);
      int heightSize = MeasureSpec.getSize(heightMeasureSpec);

      //获取导航Item的最大宽度 和 导航Item的高度总和
      float itemMaxWidth = 0f;
      float itemMaxHeight = 0f;
      for (Pair pair : mLetters) {
        Object first = pair.first;
        float temp = measureItemWidth(first);
        if (temp > itemMaxWidth) {
          itemMaxWidth = temp;
        }

        itemMaxHeight += measureItemHeight(first);
      }// end for

      //计算最小需求宽度
      final int iItemMaxWidth = (int) itemMaxWidth + 1;
      final int xpad = getPaddingLeft() + getPaddingRight();
      final int ypad = getPaddingTop() + getPaddingBottom();
      final int minWidth = iItemMaxWidth + xpad;
      switch (widthMode) {
        case MeasureSpec.AT_MOST://这个是 wrap_content
          width = Math.min(minWidth, widthSize);
          break;
        case MeasureSpec.UNSPECIFIED://这是未知
          width = minWidth;
          break;
        case MeasureSpec.EXACTLY://这个是match_parent或者指定了具体的数值
          width = widthSize;
          break;
      }

      //计算最小需求高度
      final int letterSize = mLetters.size();//获取导航item个数
      final int allLetterSpace = (letterSize - 1) * mLetterSpace;
      final int iItemMaxHeight = (int) itemMaxHeight + 1;
      final int minHeight = iItemMaxHeight + allLetterSpace + ypad;
      switch (heightMode) {
        case MeasureSpec.AT_MOST://这个是 wrap_content
          height = Math.min(minHeight, heightSize);
          break;
        case MeasureSpec.UNSPECIFIED://这是未知
          height = minHeight;
          break;
        case MeasureSpec.EXACTLY://这个是match_parent或者指定了具体的数值
          height = heightSize;
          break;
      }

      //设置View尺寸
      setMeasuredDimension(width, height);
    }
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    mBgDrawable.setBounds(0, 0, w, h);
    mLetterPaint.setTextSize(mLetterFontSize);

    int xpading = getPaddingLeft() + getPaddingRight();
    int contentHeight = w - xpading;

    mLetterLeftX = getPaddingLeft();
    mLetterTopY = getPaddingTop();
    mLetterRightX = mLetterLeftX + contentHeight;
  }

  @Override protected void onDraw(Canvas canvas) {
    if (mShowTouchBg && mInTouched) {
      mBgDrawable.draw(canvas);
    }

    /**
     * 设置绘制文字时起始点X坐标的位置
     * CENTER:以文字的宽度的中心点为起始点向两边绘制
     * LEFT:以文字左边为起始点向右边开始绘制
     * RIGHT:以文字宽度的右边为起始点向左边绘制
     */
    mLetterPaint.setTextAlign(Paint.Align.CENTER);
    mLetterPaint.setFakeBoldText(true);
    for (int i = 0; i < mLetters.size(); i++) {
      Pair<Object, Object> one = mLetters.get(i);
      if (i == 0) {
        mLetterTopY = getPaddingTop();
      }

      Object first = one.first;
      if (first instanceof String || first instanceof Character) {
        if (i == mChoose && mInTouched) {
          mLetterPaint.setColor(mTouchedLetterColor);
        } else {
          mLetterPaint.setColor(mNormalLetterColor);
        }
        Paint.FontMetrics fontMetrics = mLetterPaint.getFontMetrics();
        float textBoundCenterX = (mLetterLeftX + mLetterRightX) / 2;
        canvas.drawText(first.toString(), textBoundCenterX,
            Math.abs(fontMetrics.ascent) + mLetterTopY, mLetterPaint);
      } else {
        // FIXME: 2016/6/17 lzf 这里以后还可以添加绘制图片
      }
      mItemTopY[i] = mLetterTopY;
      //更新mLetterTopY
      mLetterTopY = mLetterTopY + mLetterSpace + measureItemHeight(first);

      mLastItemBomY = mLetterTopY;
    }// end for
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    if (null == mLetters || mLetters.size() == 0) {
      return false;
    }
    final int action = event.getAction() & MotionEvent.ACTION_MASK;
    final float y = event.getY();

    int newChoose = -1;
    for (int i = 0; i < mItemTopY.length; i++) {
      final float itemTopY = mItemTopY[i];
      float visiItemTopY = itemTopY - mLetterSpace / 2;
      if (visiItemTopY < 0) {//当i==0并且topPadding<mLetterSpace/2 时小于0
        visiItemTopY = 0;
      }
      if (visiItemTopY > y) {//取它上一个item就是当前触摸到的item
        int touchedItemPosition = i - 1;
        if (touchedItemPosition >= 0) {
          newChoose = touchedItemPosition;
        }
        break;
      }

      if (i == mItemTopY.length - 1 && y < mLastItemBomY) {//当是最后一项时
        newChoose = i;
      }
    }

    switch (action) {
      case MotionEvent.ACTION_DOWN:
        mInTouched = true;
        if (-1 != newChoose) {
          mChoose = newChoose;
          if (null != mOnItemSelected) {
            final Pair pair = mLetters.get(newChoose);
            mOnItemSelected.onSelected(newChoose, pair.first, pair.second);
          }
          invalidate();
        }
        break;
      case MotionEvent.ACTION_MOVE:
        if (-1 != newChoose && mChoose != newChoose) {
          mChoose = newChoose;
          if (null != mOnItemSelected) {
            final Pair pair = mLetters.get(newChoose);
            mOnItemSelected.onSelected(newChoose, pair.first, pair.second);
          }
          invalidate();
        }
        break;
      case MotionEvent.ACTION_UP:
        mInTouched = false;
        mChoose = -1;
        invalidate();
        break;
      case MotionEvent.ACTION_CANCEL:
        mInTouched = false;
        mChoose = -1;
        invalidate();
        break;
    }

    return true;
  }

  private void init(Context context) {
    //抗锯齿
    mLetterPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    mTextPaintTemp = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    if (-1 == mTouchBgColor) {
      mTouchBgColor = BG_COLOR;
    }
    if (-1 == mLetterFontSize) {
      DisplayMetrics dm = context.getResources().getDisplayMetrics();
      mLetterFontSize = (int) (dm.density * LETTER_FONT_SIZE);
    }
    mBgDrawable = new BgDrawable(mTouchBgColor, mBgRx, mBgRy);

    if (null == mLetters) {
      mLetters = new ArrayList<>();
      mItemTopY = new float[0];
    }
  }

  private void parseAttrs(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    if (null == attrs) {
      return;
    }
    TypedArray a = context.getTheme()
        .obtainStyledAttributes(attrs, R.styleable.LetterNavigation, defStyleAttr, defStyleRes);
    try {
      mLetterFontSize =
          a.getDimensionPixelSize(R.styleable.LetterNavigation_letterSize, LETTER_FONT_SIZE);
      mLetterSpace =
          a.getDimensionPixelSize(R.styleable.LetterNavigation_letterSpace, LETTER_SPACE);
      mTouchBgColor = a.getColor(R.styleable.LetterNavigation_touchBgColor, BG_COLOR);
      mNormalLetterColor =
          a.getColor(R.styleable.LetterNavigation_normalLetterColor, NORMAL_LETTER_COLOR);
      mTouchedLetterColor =
          a.getColor(R.styleable.LetterNavigation_touchedLetterColor, TOUCHED_LETTER_COLOR);
      mShowTouchBg = a.getBoolean(R.styleable.LetterNavigation_showTouchBg, SHOW_TOUCHED_BG_COLOR);

      mBgRx = a.getFloat(R.styleable.LetterNavigation_touchedBgRx, BG_RX);
      mBgRy = a.getFloat(R.styleable.LetterNavigation_touchedBgRy, BG_RY);

      final String letters = a.getString(R.styleable.LetterNavigation_letters);
      if (null != letters) {
        String[] letterArray = letters.split(REGULAR);
        mLetters = new ArrayList<>(letterArray.length);
        mItemTopY = new float[letterArray.length];
        for (String letter : letterArray) {
          Pair<Object, Object> pair = new Pair<Object, Object>(letter, null);
          mLetters.add(pair);
          if (DEBUG) {
            Log.e(TAG, "导航字母：" + pair.first);
          }
        }
      }
    } finally {
      a.recycle();
    }
  }

  private float measureTextWidth(String text, float textSize) {
    mTextPaintTemp.setTextSize(textSize);
    return mTextPaintTemp.measureText(text, 0, text.length());
  }

  private float measureTextHeight(float textSize) {
    mTextPaintTemp.setTextSize(textSize);
    Paint.FontMetrics fontMetrics = mTextPaintTemp.getFontMetrics();

    float ascent = fontMetrics.ascent;
    float descent = fontMetrics.descent;
    return descent - ascent;
  }

  private float measureItemHeight(Object item) {
    if (item instanceof String || item instanceof Character) {
      return measureTextHeight(mLetterFontSize);
    }
    return 0f;
  }

  private float measureItemWidth(Object item) {
    if (item instanceof String || item instanceof Character) {
      return measureTextWidth(item.toString(), mLetterFontSize);
    }
    return 0f;
  }

  /** 背景Drawable */
  class BgDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int color = -1;

    private RectF rectF;
    private float rx;
    private float ry;

    /**
     * @param color 背景色值
     * @param rx 背景x轴上圆角率
     * @param ry 背景y轴上圆角率
     */
    BgDrawable(int color, float rx, float ry) {
      this.color = color;
      paint.setColor(color);
      this.rx = rx;
      this.ry = ry;
    }

    @Override public void draw(Canvas canvas) {
      if (-1 != color) {
        if (null == rectF) {
          rectF = new RectF(getBounds());
        }
        canvas.drawRoundRect(rectF, rx, ry, paint);
      } else {
        canvas.drawColor(Color.TRANSPARENT);
      }
    }

    @Override public void setAlpha(int alpha) {

    }

    @Override public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override public int getOpacity() {
      return PixelFormat.TRANSLUCENT;
    }
  }// end class BgDrawable
}
