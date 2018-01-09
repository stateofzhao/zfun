package com.diagramsf.core.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.diagramsf.core.R;

/** 能够显示月食动画 */
public class SunMoveCircleImageView extends ImageView {

    private static final ScaleType SCALE_TYPE = ScaleType.CENTER_INSIDE;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 1;

    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();

    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;

    private float mDrawableRadius;
    private float mBorderRadius;

    private boolean mReady;
    private boolean mSetupPending;

    //=====================自己添加的
    private Path mPath = new Path();
    private float mTranslateX;

    public SunMoveCircleImageView(Context context) {
        super(context);
        initCirclePath();
    }

    public SunMoveCircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SunMoveCircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setScaleType(SCALE_TYPE);
        initCirclePath();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SunMoveCircleImageView, defStyle, 0);
        mBorderWidth = a.getDimensionPixelSize(R.styleable.SunMoveCircleImageView_sciv_border_width, DEFAULT_BORDER_WIDTH);
        mBorderColor = a.getColor(R.styleable.SunMoveCircleImageView_sciv_border_color, DEFAULT_BORDER_COLOR);
        a.recycle();

        mReady = true;
        if (mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    private void initCirclePath() {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
            //由于使用了Canvas.clipPath() 在硬件加速的情况下 再重新显示时会出现闪烁的情况，所以这里使用软件加速
            ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, null);
        }
    }

    /** 执行月食动画 在X轴上平移 */
    public void updateTranslateX(float translateX) {
        mTranslateX = translateX;
        invalidate();
    }

    @Override
    public ScaleType getScaleType() {
        return SCALE_TYPE;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != SCALE_TYPE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
        }
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }

        if (mBorderWidth != 0) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, mBorderRadius, mBorderPaint);
        }

        //进行画布的移动,由于描边在画布移动之前执行，所以这里不用让画布 save()
        //        final int saveCount = canvas.save();//这里一定要保存一下，否则上面的 描边 也会跟着画布移动，达不到在 描边 里面移动的效果
        //剪切画布(达到剪切以外的区域不会显示)
        mPath.reset();
        mPath.addCircle(getWidth() / 2, getHeight() / 2, mBorderRadius, Path.Direction.CW);
        canvas.clipPath(mPath);
        canvas.translate(mTranslateX, 0);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mDrawableRadius, mBitmapPaint);
        //        canvas.restoreToCount(saveCount);//其实这个方法如果在最后位置也可以不调用

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int borderColor) {
        if (borderColor == mBorderColor) {
            return;
        }

        mBorderColor = borderColor;
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }

    public int getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        if (borderWidth == mBorderWidth) {
            return;
        }

        mBorderWidth = borderWidth;
        setup();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFromDrawable(drawable);
        setup();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mBitmap = getBitmapFromDrawable(getDrawable());
        setup();
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    private void setup() {
        if (!mReady) {
            mSetupPending = true;
            return;
        }

        if (mBitmap == null) {
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();

        mBorderRect.set(0, 0, getWidth(), getHeight());
        mBorderRadius = Math.min((mBorderRect.height() - mBorderWidth) / 2, (mBorderRect.width() - mBorderWidth) / 2);

        mDrawableRect.set(mBorderWidth, mBorderWidth, mBorderRect.width() - mBorderWidth, mBorderRect.height() - mBorderWidth);
        mDrawableRadius = Math.min(mDrawableRect.height() / 2, mDrawableRect.width() / 2);

        updateShaderMatrix();
        invalidate();
    }

    private void updateShaderMatrix() {
        float scale = 1f;
        float dx;
        float dy;

        mShaderMatrix.set(null);
        //        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
        //            scale = mDrawableRect.height() / (float) mBitmapHeight;
        //            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        //        } else {
        //            scale = mDrawableRect.width() / (float) mBitmapWidth;
        //            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        //        }
        dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth, (int) (dy + 0.5f) + mBorderWidth);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

}