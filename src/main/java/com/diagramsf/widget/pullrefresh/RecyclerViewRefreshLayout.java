package com.diagramsf.widget.pullrefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * 专门针对{@link android.support.v7.widget.RecyclerView} 优化的下拉刷新 .
 * <p/>
 * 注意：1. ContentView (也就是第二个ChildView)必须是RecyclerView，并且RecyclerView的背景必须是透明的.
 */
public class RecyclerViewRefreshLayout extends PullRefreshLayout {
    private boolean mContentViewIsRecyclerView = false;//ContentView是否是RecyclerView
    private boolean mFirstScrollToPosition = true;
    private boolean mUserSuperConfig = false;

    private int mInitPaddingTop = 0;
    private int mInitPaddingBom = 0;
    private int mInitPaddingLeft = 0;
    private int mInitPaddingRight = 0;

    private int mInitFirstViewGetTop = 0;//RecyclerView中第一个View距离AbsList顶部的初始位置

    public RecyclerViewRefreshLayout(Context context) {
        super(context);
    }

    public RecyclerViewRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecyclerViewRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 禁用{@link RecyclerViewRefreshLayout}重写的一切方法，使用{@link PullRefreshLayout}中定义的方法
     *
     * @param userSuperConfig true禁用；false不禁用
     */
    public void setUserSuperConfig(boolean userSuperConfig) {
        mUserSuperConfig = userSuperConfig;
    }

    @Override
    protected void contentViewScrollToPosition(boolean isRefreshToNormal,
                                               float yDirection, int x, int y) {
        if (isGoodDoAbsListPadding()) {
            if (mFirstScrollToPosition) {
                initData(mContentView);
                mFirstScrollToPosition = false;
            }

            final RecyclerView recyclerView = (RecyclerView) mContentView;
            recyclerView.setPadding(mInitPaddingLeft, mInitPaddingTop + Math.abs(y), mInitPaddingRight, mInitPaddingBom);
            if (!isRefreshToNormal) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                layoutManager.scrollToPosition(0);
            }
        } else {
            super.contentViewScrollToPosition(isRefreshToNormal, yDirection, x, y);
        }

    }

    @Override
    protected int getContentViewOffsetFromTop() {
        if (isGoodDoAbsListPadding()) {
            return -mContentView.getPaddingTop() + mInitPaddingTop;
        } else {
            return super.getContentViewOffsetFromTop();
        }
    }

    @Override
    protected boolean isContentViewOffsetFromTop() {
        if (isGoodDoAbsListPadding()) {
            return mContentView.getPaddingTop() - mInitPaddingTop > 0;
        } else
            return super.isContentViewOffsetFromTop();
    }

    @Override
    protected boolean customContentViewAchieveEvent(float yDirection,
                                                    State state) {
        if (isGoodDoAbsListPadding()) {
            if (state == State.REFRESHING && yDirection < 0) {
                final int offsetTop = mContentView.getPaddingTop() - mInitPaddingTop;
                if (offsetTop == getRefreshHeight()) {
                    return true;
                }
            }
        }

        return super.customContentViewAchieveEvent(yDirection, state);
    }

    @Override
    protected void onInitContentView(View contentView) {
        super.onInitContentView(contentView);
        mFirstScrollToPosition = true;
        if ((contentView instanceof RecyclerView)) {

            RecyclerView recyclerView = (RecyclerView) contentView;

            if (osAboveAPI10()) {
                initData(contentView);

                recyclerView.setClipToPadding(false);
            }

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (null == mRefreshHeaderView) {
                        return;
                    }

                    if (!mUserSuperConfig && mState == State.REFRESHING) {
                        if (recyclerView.getChildCount() == 0 || recyclerView.getChildAt(0).getTop() <= mInitFirstViewGetTop) {
                            mRefreshHeaderView.setVisibility(View.INVISIBLE);
                        } else {
                            mRefreshHeaderView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

            mContentViewIsRecyclerView = true;
        } else {
            mContentViewIsRecyclerView = false;
        }

    }

    @Override
    public boolean canChildScrollUp(View view) {
        if (view instanceof RecyclerView) {
            final RecyclerView.LayoutManager layoutManager = ((RecyclerView) view).getLayoutManager();
            if (null == layoutManager) {
                return super.canChildScrollUp(view);
            }

            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                // 获取第一个完全显示的item position
                final int firstCompletelyVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                if ((firstCompletelyVisibleItemPosition == 0 && view.getPaddingTop() <= mInitPaddingTop)
                        || linearLayoutManager.getItemCount() == 0) {
                    return false;
                }
            }

        }
        return super.canChildScrollUp(view);
    }

    private boolean osAboveAPI10() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    private boolean isGoodDoAbsListPadding() {
        return osAboveAPI10() && mContentViewIsRecyclerView && !mUserSuperConfig;
    }

    private void initData(View contentView) {
        mInitPaddingTop = contentView.getPaddingTop();
        mInitPaddingBom = contentView.getPaddingBottom();
        mInitPaddingLeft = contentView.getPaddingLeft();
        mInitPaddingRight = contentView.getPaddingRight();
        if (null != ((RecyclerView) contentView).getChildAt(0)) {
            mInitFirstViewGetTop = ((RecyclerView) contentView).getChildAt(0).getTop();
        }
    }
}
