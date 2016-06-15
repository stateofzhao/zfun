package com.diagramsf.customview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import com.diagramsf.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 字母导航条
 * <p/>
 * Created by Diagrams on 2016/6/15 14:43
 */
public class LetterNavigation extends View {

    private final static String REGULAR = "%$";

    private final static int LETTER_SIZE = 15;//dp
    private final static int BG_COLOR = Color.TRANSPARENT;

    private Map<Object, Object> mLetters;//要显示的字母导航集合
    private int mLetterSize = -1;
    private int mBgColor = -1;
    private float mBgRx = 0f;
    private float mBgRy = 0f;

    private Paint mLetterPaint;
    private Paint mBackGroudPaint;

    private BgDrawable mBgDrawable;

    private Rect mBound;//本View的矩形尺寸

    private boolean mShowBg;

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureSize(widthMeasureSpec, heightMeasureSpec);

        mBgDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mBgDrawable.draw(canvas);
    }

    private void init(Context context) {
        //抗锯齿
        mLetterPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        //抗锯齿
        mBackGroudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (-1 == mBgColor) {
            mBgColor = BG_COLOR;
        }
        if (-1 == mLetterSize) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            mLetterSize = (int) (dm.density * LETTER_SIZE);
        }
        mBgDrawable = new BgDrawable(mBgColor, mBgRx, mBgRy);
    }

    private void parseAttrs(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (null == attrs) {
            return;
        }
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LetterNavigation,
                defStyleAttr, defStyleRes);
        try {
            mLetterSize = a.getDimensionPixelSize(R.styleable.LetterNavigation_letterSize, LETTER_SIZE);
            mBgColor = a.getColor(R.styleable.LetterNavigation_bgColor, BG_COLOR);

            final String letters = a.getString(R.styleable.LetterNavigation_letters);
            if (null != letters) {
                String[] letterArray = letters.split(REGULAR);
                mLetters = new HashMap<>(letterArray.length);
                for (String letter : letterArray) {
                    mLetters.put(letter, null);
                }
            }
        } finally {
            a.recycle();
        }
    }

    //TODO 需要解决，当是宽和高都是 wrap_content 时，如何根据 text 来确定自身尺寸
    private void measureSize(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heighMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heighSize = MeasureSpec.getSize(heightMeasureSpec);

    }

    /** 背景Drawable */
    class BgDrawable extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int color = -1;

        private RectF rectF;
        private float rx;
        private float ry;

        private boolean show;

        /**
         * @param color 背景色值
         * @param rx    背景x轴上圆角率
         * @param ry    背景y轴上圆角率
         */
        public BgDrawable(int color, float rx, float ry) {
            this.color = color;
            paint.setColor(color);
            this.rx = rx;
            this.ry = ry;
        }

        public void show(boolean show) {
            if (this.show != show) {
                this.show = show;
                invalidateSelf();
            }

        }

        @Override
        public void draw(Canvas canvas) {
            if (-1 != color) {
                if (null == rectF) {
                    rectF = new RectF(getBounds());
                }
                canvas.drawRoundRect(rectF, rx, ry, paint);
            }
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

}
