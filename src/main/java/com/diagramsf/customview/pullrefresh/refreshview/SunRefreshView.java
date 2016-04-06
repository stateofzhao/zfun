package com.diagramsf.customview.pullrefresh.refreshview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.diagramsf.customview.pullrefresh.PullRefreshLayout;
import com.diagramsf.customview.pullrefresh.RefreshViewCallback;
import com.diagramsf.helpers.UIHelper;

public class SunRefreshView extends View implements RefreshViewCallback {

    private final static float REFRESH_HEIGHT = 120;// dp

    private SunRefreshDrawable mDrawable;

    private boolean mHasDoRefresh = false;

    public SunRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SunRefreshView(Context context) {
        super(context);
        init();
    }

    public SunRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        final int refreshHeight = (int) UIHelper.convertDpToPixel(getContext(),
                REFRESH_HEIGHT);
        mDrawable = new SunRefreshDrawable(getContext(), this, refreshHeight);

        mDrawable.offsetTopAndBottom(refreshHeight + 80);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = mDrawable.getTotalDragDistance() * 6 / 4;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height
                + getPaddingTop() + getPaddingBottom(), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        int pl = getPaddingLeft();
        int pt = getPaddingTop();
        mDrawable.setBounds(pl, pt, pl + right - left, pt + bottom - top);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

    @Override
    public void invalidateDrawable(Drawable dr) {
        if (dr == mDrawable) {
            invalidate();
        } else {
            super.invalidateDrawable(dr);
        }
    }

    @Override
    public int getRefreshHeight() {
        // Log.v(VIEW_LOG_TAG, "刷新高度：" + getMeasuredHeight());
        return getMeasuredHeight();
    }


    @Override
    public void doRefresh() {
        mDrawable.start();
        mHasDoRefresh = true;
    }

    @Override
    public void refreshStop() {
        mDrawable.stop();
        invalidate();
    }

    @Override
    public void contentViewScrollDistance(int distance,PullRefreshLayout.State state) {

        if (!mHasDoRefresh) {
            float percent = (float) distance / getRefreshHeight();
            mDrawable.setPercent(percent);
            invalidate();
        }

    }

    @Override
    public void contentViewBeginScroll() {
        mDrawable.resetOriginals();
        invalidate();
        mHasDoRefresh = false;
    }

    @Override
    public void contentViewEndScroll() {
        mDrawable.stop();
        invalidate();
    }

}
