package com.zfun.lib.widget.xpullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.*;

import com.zfun.lib.BuildConfig;

/**
 * 采用NestedScroll结构实现的新版下拉刷新。
 * <p>
 * Created by zfun on 2021/11/29 11:28 上午
 */
public class XPullRefreshLayout extends FrameLayout implements NestedScrollingParent3, NestedScrollingChild3 {
    public interface OnRefreshListener {
        void onRefresh();
    }//

    public interface OnPageChangeCallback{
        void onScrollToPage(int pos);
        void onPageSelected(int pos);
    }//

    private static final String TAG = "XPullRefreshLayout";
    private static final int MIN_FLING_VELOCITY = 400; // dips
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips

    public static final int HEADER_STATE_NORMAL = 1;
    public static final int HEADER_STATE_REFRESH = 2;
    //
    public static final int STYLE_PULL_REFRESH = 1;
    public static final int STYLE_VERTICAL_PAGE = 2;

    private int mStyle = STYLE_PULL_REFRESH;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    //STYLE_VERTICAL_PAGE
    private int mCurPagePos = 0;
    private int mFlingDistance;

    //STYLE_PULL_REFRESH
    private int mCurHeaderState = HEADER_STATE_NORMAL;//Header当前状态
    private int mHeaderPullRefreshLimit = 0;//下拉多少距离能够触发刷新状态
    private int mHeaderSpringBackAnimTimeMs = 500;

    private OnRefreshListener mOnRefreshListener;
    private OnPageChangeCallback mOnPageChangeCallback;

    private boolean mIsBeingDragged = false;
    private boolean mIsInTouchMotion = false;

    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;

    private NestedScrollingChildHelper mChildHelper;
    private NestedScrollingParentHelper mParentHelper;

    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private final int[] mScrollOffsetCompat = new int[2];
    private final int[] mScrollConsumedCompat = new int[2];
    private int mNestedYOffset;

    private int mLastY;
    private int mInitTouchScrollY;

    public XPullRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public XPullRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XPullRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initXPullRefreshLayout(context);
    }

    public void setRefreshEnd() {
        if (mCurHeaderState != HEADER_STATE_REFRESH) {
            return;
        }
        mScroller.abortAnimation();
        mCurHeaderState = HEADER_STATE_NORMAL;
        //方案一,结束刷新时，如果有头View漏出来了且在TOUCH中，不强制回滚：
        if (!mIsInTouchMotion) {
            handleSpringBackIfNeed(0);
        }

        //方案二，结束刷新时，如果有头View漏出来了，且在TOUCH，强制回滚：
        //如果触发了回滚，向子View发送 ACTION_CANCEL 事件，下面这种逻辑暂时有点问题，待以后完善吧
        /*final boolean isNeedSpringBack = handleHeaderSpringBackIfNeed();
        if (isNeedSpringBack) {
            final int childCount = getChildCount();
            if (childCount > 0) {
                final long now = SystemClock.uptimeMillis();
                final MotionEvent event = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
                event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                for (int i = 0; i < childCount; i++) {
                    final View child = getChildAt(i);
                    child.dispatchTouchEvent(event);
                }
                event.recycle();
            }
        }*/
    }

    public void setParams(int pullRefreshLimit, int springBackAnimTimeMs) {
        if (mCurHeaderState == HEADER_STATE_REFRESH) {//刷新状态下不能更改
            return;
        }
        mHeaderPullRefreshLimit = pullRefreshLimit;
        mHeaderSpringBackAnimTimeMs = springBackAnimTimeMs;
    }

    /**
     * @param style {@link #STYLE_PULL_REFRESH}、{@link #STYLE_VERTICAL_PAGE}
     * */
    public void setStyle(int style){
        if(style != STYLE_PULL_REFRESH && style != STYLE_VERTICAL_PAGE){
            return;
        }
        mStyle = style;
        requestLayout();
    }
    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }

    public void setOnPageChangeCallback(OnPageChangeCallback callback){
        mOnPageChangeCallback = callback;
    }



    private void internalLayoutChild() {
        if (getChildCount() > 0) {
            if (mStyle == STYLE_PULL_REFRESH) {
                //第一个View为headerView，调整下位置
                View headView = getChildAt(0);
                headView.layout(headView.getLeft(), headView.getTop() - headView.getHeight(), headView.getRight(), headView.getBottom() - headView.getHeight());
            } else if (mStyle == STYLE_VERTICAL_PAGE) {
                final int childCount = getChildCount();
                final int myHeight = getMeasuredHeight();
                int lastChildBom = 0;
                for (int i = 0; i < childCount; i++){
                    final View aView = getChildAt(i);
                    final int top = aView.getTop()+lastChildBom;
                    lastChildBom = top + myHeight;
                    aView.layout(aView.getLeft(),top,aView.getRight(),lastChildBom);
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        internalLayoutChild();
        scrollTo(getScrollX(), getScrollY());
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        /*Log.e(TAG, "ViewScrollTo：" + scrollY);*/
        super.scrollTo(0, scrollY);//scrollY 为正，自己向上滚动；为负，自己向下滚动
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            mInitTouchScrollY = getScrollY();
            mIsInTouchMotion = true;
        } else if (action == MotionEvent.ACTION_UP) {
            changeHeaderStateRefreshIfNeed();
            handleSpringBackIfNeed(0);
            mIsInTouchMotion = false;
        } else if (action == MotionEvent.ACTION_CANCEL) {
            handleSpringBackIfNeed(0);
            mIsInTouchMotion = false;
        }
        return super.dispatchTouchEvent(ev);
    }

    //touch事件只是为了实现 NestedScrollingChild 功能
    //只负责何时进行拦截
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) ev.getY();
                mScroller.computeScrollOffset();
                debugLog("onInterceptTouchEvent = ACTION_DOWN = " + mScroller.isFinished());
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                /*mScroller.computeScrollOffset();
                Log.e(TAG, "onInterceptTouchEvent = ACTION_DOWN = "+mScroller.isFinished());
                if (mScroller.isFinished()) {
                    mIsBeingDragged = false;
                } else {
                    mIsBeingDragged = true;
                }*/
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
                break;
            case MotionEvent.ACTION_MOVE:
                debugLog("onInterceptTouchEvent = ACTION_MOVE == getNestedScrollAxes() = " + getNestedScrollAxes());
                int y = (int) ev.getY();
                int yDiff = Math.abs(y - mLastY);
                debugLog("onInterceptTouchEvent = yDiff    = " + yDiff);
                debugLog("onInterceptTouchEvent = mTouchSlop= " + mTouchSlop);
                if (yDiff > mTouchSlop && (getNestedScrollAxes() & ViewCompat.SCROLL_AXIS_VERTICAL) == ViewCompat.SCROLL_AXIS_NONE) {
                    mLastY = y;
                    mIsBeingDragged = true;
                    mNestedYOffset = 0;

                    //其实这个也可以放到 onTouchEvent() 的 MotionEvent.ACTION_MOVE 中，但是为了不频繁调用，就需要再用个变量判断，
                    // 放到这里没问题，因为一旦 mIsBeingDragged==true，就 return true了，那么就会转而执行 onTouchEvent() 方法，不再执行此方法了。
                    disAllowParentIntercept(true);
                    recycleVelocityTracker();
                }
                debugLog("onInterceptTouchEvent = ACTION_MOVE == mIsBeingDragged = " + mIsBeingDragged);
                if (mIsBeingDragged) {
                    initVelocityTrackerNoExist();
                    mVelocityTracker.addMovement(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                debugLog("onInterceptTouchEvent = ACTION_CANCEL|ACTION_UP");
                mIsBeingDragged = false;
                stopNestedScroll(ViewCompat.TYPE_TOUCH);
                recycleVelocityTracker();
                break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final MotionEvent event = MotionEvent.obtain(ev);
        event.offsetLocation(0, mNestedYOffset);
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                debugLog("onTouchEvent - ACTION_DOWN = " + mIsBeingDragged);
                /*mIsBeingDragged = !mScroller.isFinished();
                if (mIsBeingDragged) {
                    mScroller.abortAnimation();
                    disAllowParentIntercept(true);
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
                }*/
                mNestedYOffset = 0;
                mIsBeingDragged = true;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                disAllowParentIntercept(true);
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH);
                mLastY = (int) event.getY();
                initOrClearTracker();
                break;
            case MotionEvent.ACTION_MOVE:
                final int y = (int) event.getY();
                int yDelta = y - mLastY;
                debugLog("onTouchEvent - 手指滑动 yDelta = " + yDelta);
                //处理嵌套滚动
                boolean dispatchScroll = dispatchNestedPreScroll(0, yDelta, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_TOUCH);
                if (dispatchScroll) {
                    yDelta = yDelta - mScrollConsumed[1];
                    mNestedYOffset += mScrollOffset[1];
                    event.offsetLocation(0, mScrollOffset[1]);//在开始偏移的基础上再次偏移
                }
                if (!mIsBeingDragged && Math.abs(yDelta) > mTouchSlop) {//首次触发滚动处理，需要做一些初始化操作
                    mIsBeingDragged = true;
                    disAllowParentIntercept(true);
                    //判断是否可以滚动的这个距离去掉，mTouchSlop一定 >0
                    if (yDelta > 0) {
                        yDelta -= mTouchSlop;
                    } else {
                        yDelta += mTouchSlop;
                    }
                    recycleVelocityTracker();
                }
                if (mIsBeingDragged) {
                    initVelocityTrackerNoExist();
                    mLastY = (int) event.getY();
                    //处理自己的滚动
                    final int oldY = getScrollY();
                    final int yRange = getYScrollRange();
                    boolean isScroll2Limit = handleSelfScroll(-yDelta, oldY, yRange);//处理自己的滚动
                    if (isScroll2Limit) {
                        mVelocityTracker.clear();
                    }
                    final int scrollDeltaY = getScrollY() - oldY;
                    final int unconsumedY = yDelta - scrollDeltaY;
                    dispatchNestedScroll(0, scrollDeltaY, 0, unconsumedY, mScrollOffset, ViewCompat.TYPE_TOUCH, mScrollConsumed);
                    //更新下touch偏移
                    mNestedYOffset += mScrollOffset[1];
                    mLastY -= mScrollOffset[1];
                    event.offsetLocation(0, mScrollOffset[1]);
                }
                break;
            case MotionEvent.ACTION_UP:
                //放到 dispatchTouchEvent() 中处理了
                /*changeHeaderStateRefreshIfNeed();
                handleHeaderSpringBackIfNeed();*/
                if (isCanFling()) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    final int velocityY = (int) mVelocityTracker.getYVelocity();
                    handleFlingWithNestedScroll(velocityY);
                }
                endDrag();
                break;
            case MotionEvent.ACTION_CANCEL:
                //放到 dispatchTouchEvent() 中处理了
                /*handleHeaderSpringBackIfNeed();
                 * handleHeaderSpringBackIfNeed();*/
                endDrag();
                break;
        }
        if (null != mVelocityTracker) {
            mVelocityTracker.addMovement(event);
        }
        event.recycle();
        return true;
    }

    @Override
    public void computeScroll() {
        if (!mScroller.computeScrollOffset()) {
            if (mStyle == STYLE_PULL_REFRESH) {
                if (hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH)) {//有可能是回弹头View触发的Scroller
                    stopNestedScroll(ViewCompat.TYPE_NON_TOUCH);
                }
            } else if (mStyle == STYLE_VERTICAL_PAGE) {
                final int scrollY = getScrollY();
                final int pageHeight = getHeight();
                final boolean isSopInPage = scrollY % pageHeight == 0;
                if(isSopInPage){
                    final int nowPos = scrollY/pageHeight;
                    setCurrPagePos(nowPos,true);
                }
            }
            return;
        }
        debugLog("XPull-SCROLLER", "computeScroll");
        final int oldScrollY = getScrollY();
        final int scrollY = mScroller.getCurrY();
        int yDelta = scrollY - oldScrollY;
        if (dispatchNestedPreScroll(0, yDelta, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_NON_TOUCH)) {
            yDelta -= mScrollConsumed[1];
        }
        handleSelfScroll(yDelta, oldScrollY, getYScrollRange());
        final int consumedY = getScrollY() - oldScrollY;
        dispatchNestedScroll(0, consumedY, 0, yDelta - consumedY, mScrollOffset, ViewCompat.TYPE_NON_TOUCH, mScrollConsumed);

        ViewCompat.postInvalidateOnAnimation(this);
    }

    //NestedScrollingParent --- start
    @Override
    public boolean onStartNestedScroll(View child, View target, int axes, int type) {
        boolean result;
        if(mStyle == STYLE_PULL_REFRESH){
            result = axes == ViewCompat.SCROLL_AXIS_VERTICAL;
        } else if(mStyle == STYLE_VERTICAL_PAGE){//page类型，不处理fling引起的自身滚动
            result = type != ViewCompat.TYPE_NON_TOUCH;
        } else {
            result = axes == ViewCompat.SCROLL_AXIS_VERTICAL;
        }
        debugLog("onStartNestedScroll == "+result);
        return result;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes, int type) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, type);
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mParentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onStopNestedScroll(View target, int type) {
        debugLog("onStopNestedScroll");
        mParentHelper.onStopNestedScroll(target, type);
        stopNestedScroll(type);
    }

    @Override
    public void onStopNestedScroll(View child) {
        onStopNestedScroll(child, ViewCompat.TYPE_TOUCH);
    }

    /*private int mOnNestedPreScrollConsumed = 0;*/

    //优先消耗子View的滚动值
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
        debugLog("onNestedPreScroll");
        if(mStyle == STYLE_PULL_REFRESH){
            final int oldScrollY = getScrollY();
            if (oldScrollY < 0) {
                handleSelfScroll(dy, oldScrollY, getYScrollRange());
            }
            int consumedY = getScrollY() - oldScrollY;
            consumed[1] += consumedY;
            dispatchNestedPreScroll(dx, dy, consumed, mScrollOffset, type);
        } else if(mStyle == STYLE_VERTICAL_PAGE){
            final int oldScrollY = getScrollY();
            final int pageHeight = getHeight();
            if((oldScrollY % pageHeight != 0 )){//未滚动到整页则需要消耗掉子View的滚动事件来让自己滚动
                handleSelfScroll(dy, oldScrollY, getYScrollRange());
            }
            int consumedY = getScrollY() - oldScrollY;
            consumed[1] += consumedY;
            dispatchNestedPreScroll(dx, dy, consumed, mScrollOffset, type);
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        mScrollConsumedCompat[0] = 0;
        mScrollConsumedCompat[1] = 0;
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, mScrollConsumedCompat);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, int[] consumed) {
        debugLog("onNestedScroll = dyUnconsumed =" + dyUnconsumed);
        //处理自己的嵌套滚动逻辑
        if(mStyle == STYLE_PULL_REFRESH){
            if (type == ViewCompat.TYPE_NON_TOUCH && mCurHeaderState == HEADER_STATE_NORMAL) {//normal时无需处理子View的联合fling
                mScrollOffsetCompat[0] = 0;
                mScrollOffsetCompat[1] = 0;
                dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mScrollOffsetCompat, type, consumed);
                return;
            }
        } else if(mStyle == STYLE_VERTICAL_PAGE){//总是处理子View不要的滚动事件
        }

        final int oldY = getScrollY();
        final int yScrollRange = getYScrollRange();
        handleSelfScroll(dyUnconsumed, oldY, yScrollRange);
        final int myConsumed = getScrollY() - oldY;
        if (null != consumed) {
            consumed[1] += myConsumed;
        }
        mScrollOffsetCompat[0] = 0;
        mScrollOffsetCompat[1] = 0;
        dispatchNestedScroll(dxConsumed, dyConsumed + myConsumed, dxUnconsumed, dyUnconsumed - myConsumed, mScrollOffsetCompat, type, consumed);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        debugLog("onNestedFling ==consumed== "+consumed);
        if (!consumed) {
            handleFlingWithNestedScroll((int) velocityY);
            return true;
        }
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        debugLog("onNestedPreFling");
        //如果 正在/将要 回弹头View，阻止fling貌似更合理？
        mScroller.computeScrollOffset();
        if (!mScroller.isFinished()) {
            return true;
        }
        if (mStyle == STYLE_PULL_REFRESH) {
            if (mCurHeaderState == HEADER_STATE_NORMAL && getScrollY() != 0) {
                //未处于刷新状态，并且自身有了滚动，则阻止子View的fling来防止自己也在滚动（手指拖动）时，子View也在滚动
                return true;
            }
        } else if (mStyle == STYLE_VERTICAL_PAGE) {
            //永远不阻止子View的fling，一旦子View自身不能在Fling时，会回调 onStartNestedScroll()方法，
            // 我们可以根据情况在 onStartNestedScroll() 方法中阻止自身后续的子View fling引起的嵌套滚动。
        }
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }//NestedScrollingParent --- end

    private void initXPullRefreshLayout(Context context) {
        final float density = context.getResources().getDisplayMetrics().density;
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);

        mScroller = new OverScroller(context);
        mChildHelper = new NestedScrollingChildHelper(this);
        mParentHelper = new NestedScrollingParentHelper(this);
    }

    private void disAllowParentIntercept(boolean disAllow) {
        final ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disAllow);//禁止父View后续拦截触摸事件
        }
    }

    private void initVelocityTrackerNoExist() {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void initOrClearTracker() {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void recycleVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    //不负责主动处理内容子View的滚动（就是非NestScrollView类型的contentView的高度超出了本View的高度，本View也不回处理其滚动），
    // 但是需要主动处理拉内容子View时让 头View 露出来 or 主动让PagerView的都能露出来，如果子View是 NestScrollView 则利用其滚动回调来让PagerView露出来
    private int getYScrollRange() {
        if (mStyle == STYLE_PULL_REFRESH) {
            if (0 != mHeaderPullRefreshLimit) {
                return mHeaderPullRefreshLimit;
            }
            if (getChildCount() == 0) {
                return 0;
            }
            View headView = getChildAt(0);
            mHeaderPullRefreshLimit = headView.getHeight();
            return mHeaderPullRefreshLimit;
        } else if (mStyle == STYLE_VERTICAL_PAGE) {
            final int pageHeight = getHeight();
            return (getChildCount()-1)*pageHeight;
        }
        return 0;
    }

    private boolean isCanFling() {
        if(mStyle == STYLE_PULL_REFRESH){
            return mCurHeaderState == HEADER_STATE_REFRESH;
        }
        return false;
    }

    //注意：如果手指向下滑动此处 yDelta 需要是负值；反之为正值。
    //返回true表示滚动到了边界
    private boolean handleSelfScroll(int yDelta, int oldScrollY, int scrollYRange) {
        if(mStyle == STYLE_PULL_REFRESH){
            int limitTop = 0;
            int limitBom = -scrollYRange;
            int newScrollY = oldScrollY + yDelta;

            boolean isYScroll2Limit = false;
            if (newScrollY > limitTop) {
                newScrollY = limitTop;
                isYScroll2Limit = true;
            } else if (newScrollY < limitBom) {
                newScrollY = limitBom;
                isYScroll2Limit = true;
            }
            onOverScrolled(0, newScrollY, true, isYScroll2Limit);
            return isYScroll2Limit;
        } else if(mStyle == STYLE_VERTICAL_PAGE){
            int limitTop = scrollYRange;
            int limitBom = 0;
            int newScrollY = oldScrollY + yDelta;
            boolean isYScroll2Limit = false;
            if (newScrollY > limitTop) {
                newScrollY = limitTop;
                isYScroll2Limit = true;
            } else if (newScrollY < limitBom) {
                newScrollY = limitBom;
                isYScroll2Limit = true;
            }
            onOverScrolled(0, newScrollY, true, isYScroll2Limit);
            return isYScroll2Limit;
        }
        return true;
    }

    //这个纯粹是自己的滚动业务，无需nestScroll调用
    private boolean handleSpringBackIfNeed(int velocityY) {
        if(mStyle == STYLE_PULL_REFRESH){
            if (mCurHeaderState == HEADER_STATE_NORMAL) {
                final int scrollY = getScrollY();//<0 手指下拉，view发生了向下滚动
                if (scrollY < 0) {//需要回弹headerView
                    mScroller.startScroll(0, scrollY, 0, -scrollY, mHeaderSpringBackAnimTimeMs);
                    ViewCompat.postInvalidateOnAnimation(this);
                    return true;
                }
            }
        } else if(mStyle == STYLE_VERTICAL_PAGE){
            final int scrollY = getScrollY();//<0 手指下拉，view发生了向下滚动
            final int direction = scrollY - mInitTouchScrollY;// <0 view向下滚动了
            final int pageHeight = getHeight();
            final int pageOffset = (scrollY-pageHeight*mCurPagePos) % pageHeight;
            if(velocityY == 0){
                //fixme 这里只有自己获取了touch事件时才有速度，如果是子View嵌套滚动引起的自身fling，则没有速度
                if(null != mVelocityTracker){
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    velocityY = (int) mVelocityTracker.getYVelocity();
                }
            }
            final int nextPagePos = determineTargetPage(mCurPagePos, pageOffset, velocityY);
            final int dy = nextPagePos * pageHeight - scrollY;
            if (dy == 0) {
                setCurrPagePos(nextPagePos, true);
            } else {
                dispatchScrollToPage(nextPagePos);
                mScroller.startScroll(0, scrollY, 0, dy, mHeaderSpringBackAnimTimeMs);
                ViewCompat.postInvalidateOnAnimation(this);
                return true;
            }
        }
        return false;
    }

    private void handleFlingWithNestedScroll(int velocityY) {
        if (Math.abs(velocityY) > mMinimumVelocity) {
            if (!dispatchNestedPreFling(0, velocityY)) {//父不处理的情况下自己才处理，fixme 这里存疑：由于自身特殊性，必须自己处理，是否不需要询问父View了?
                final int scrollY = getScrollY();
                boolean canFling = false;
                if (mCurHeaderState == HEADER_STATE_NORMAL) {
                    canFling = false;//无需判断，自身肯定不会消耗fling事件
                } else {
                    int limitTop = 0;
                    int limitBom = -getYScrollRange();
                    if (velocityY > 0) {//手指下划触发的
                        if (scrollY < limitTop && scrollY > -limitBom) {
                            canFling = true;
                        }
                    } else if (velocityY < 0) {
                        if (scrollY < limitTop && scrollY > -limitBom) {
                            canFling = true;
                        }
                    }
                }
                //无论自身消耗与否都需要分发出去，以便父View能够来处理
                dispatchNestedFling(0, velocityY, canFling);
                fling(velocityY);
            }
        }
    }

    private void fling(int velocityY) {
        if (getYScrollRange() == 0) {
            return;
        }
        debugLog("fling-velocity：" + velocityY);
        //注意，ViewCompat.TYPE_NON_TOUCH 相关的 startNestedScroll() 与
        // ViewCompat.TYPE_TOUCH 相关的 startNestedScroll() 互不干扰，独立调用
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);
        mScroller.fling(0, getScrollY(), //start
                0, -velocityY, //速度
                Integer.MIN_VALUE, Integer.MAX_VALUE, //x轴的最小值和最大值
                Integer.MIN_VALUE, Integer.MAX_VALUE, //y轴
                0, 0);//回弹
        ViewCompat.postInvalidateOnAnimation(this);//触发 computeScroll() 方法
    }

    private void changeHeaderStateRefreshIfNeed() {
        if (0 == mHeaderPullRefreshLimit) {
            mHeaderPullRefreshLimit = getYScrollRange();
        }
        final int curScrollY = getScrollY();
        final int oldState = mCurHeaderState;
        if (-curScrollY >= mHeaderPullRefreshLimit) {
            mCurHeaderState = HEADER_STATE_REFRESH;
            if (oldState != mCurHeaderState) {
                if (null != mOnRefreshListener) {
                    mOnRefreshListener.onRefresh();
                }
            }
        }
    }

    private void endDrag() {
        recycleVelocityTracker();
        stopNestedScroll(ViewCompat.TYPE_TOUCH);
        mIsBeingDragged = false;
    }
    //
    //page
    private void setCurrPagePos(int pos, boolean dispatchSelected) {
        if (mCurPagePos == pos) {
            return;
        }
        mCurPagePos = pos;
        if (dispatchSelected) {
            dispatchPageSelected(pos);
        }
    }

    private int determineTargetPage(int currentPage, int pageOffset, int velocity) {
        int targetPage;
        final int pageHeight = getHeight();
        final boolean boostFlipPage = Math.abs(velocity) >= mMinimumVelocity;
        final boolean canFlipPage = boostFlipPage? (Math.abs(pageOffset) >= pageHeight / 12):(Math.abs(pageOffset) >= pageHeight / 10);
        final int flipPageIncrease;
        if (canFlipPage) {
            if (getScrollY() - currentPage * pageHeight < 0) {
                flipPageIncrease = -1;
            } else {
                flipPageIncrease = 1;
            }
        } else {
            flipPageIncrease = 0;
        }
        targetPage = currentPage + flipPageIncrease;

        if (getChildCount() > 0) {
            final int first = 0;
            final int max = getChildCount();
            targetPage = Math.max(first, Math.min(targetPage, max));
        }
        return targetPage;

        /*int targetPage;
        if (Math.abs(velocity) > mMinimumVelocity) {
            targetPage = velocity > 0 ? currentPage : currentPage + 1;
        } else {
            final float truncator = currentPage >= mCurPagePos ? 0.4f : 0.6f;
            targetPage = currentPage + (int) (pageOffset + truncator);
        }

        if (getChildCount() > 0) {
            final int first = 0;
            final int max = getChildCount();

            // Only let the user target pages we have items for
            targetPage = Math.max(first, Math.min(targetPage, max));
        }
        return targetPage;*/
    }

    private void dispatchPageSelected(int pos){
        if(null == mOnPageChangeCallback){
            return;
        }
        mOnPageChangeCallback.onPageSelected(pos);
    }

    private void dispatchScrollToPage(int pos){
        if(null == mOnPageChangeCallback){
            return;
        }
        mOnPageChangeCallback.onScrollToPage(pos);
    }

    //NestedScrollingChild ---- start
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type, @NonNull int[] consumed) {
        mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mChildHelper.startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll(int type) {
        mChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }//NestedScrollingChild ---- end

    private void debugLog(String msg){
        debugLog(TAG,msg);
    }

    private void debugLog(String tag,String msg){
        if(BuildConfig.DEBUG){
            Log.d(tag,msg);
        }
    }
}
