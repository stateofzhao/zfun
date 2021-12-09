package com.zfun.learn;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * 自定义View 需要注意的重写方法！<P>
 *
 * 自定义View或者ViewGroup都不需要重写 measure()和layout()方法！因为这两个方法由Android系统接管实现。
 * <P>
 * 总结来说，自定义View(非ViewGroup)一般实现
 * onSizeChanged(),onDraw()即可，如果你需要更好地控制你的视图的布局参数需要实现onMeasure()方法； 自定义ViewGroup
 * 需要实现 onMeasure()和 onLayout()即可。
 */
public class LearnCustomView extends ViewGroup {

  public LearnCustomView(Context context) {
    super(context);
  }

  public LearnCustomView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public LearnCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public LearnCustomView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  //这个方法获取建议的最小高度
  @Override
  protected int getSuggestedMinimumHeight() {
    return super.getSuggestedMinimumHeight();
  }

  //这个方法获取建议的最小宽度
  @Override
  protected int getSuggestedMinimumWidth() {
    return super.getSuggestedMinimumWidth();
  }

  // 非ViewGroup的View如果不做特殊处理（例如根据自定的xml属性来改变默认的测量实现），不必重写此方法。<P>

  // ViewGroup也可以不重写此方法，因为最终确定子View显示位置和大小的是onLayout()方法控制的。
  // 此方法确定的子View大小（子View.getMeasuredWidth()和子View.getMeasuredHeight()）只是给onLayout方法提供一个参考。
  // 虽然此方法不是必须的，但是一般ViewGroup还是要重写此方法，来给子View设置measureWidth和measureHeight（通过measureChildren()、
  // measureChild()或者measureChildWithMargin()来设置）<P>

  /** 可以参见{@link com.zfun.lib.widget.StackLayout}的用法 */
  // 这个方法是有两个作用：
  // 1. 通过调用setMeasuredDimension()设置自身尺寸;
  // 2. 读取子View的LayoutParams中的layout_width和layout_height 分别结合 widthMeasureSpec 和 heightMeasureSpec
  // 来设置子View的尺寸（这个过程被封装到了 View的measureChild()、measureChildren()和measureChildWithMargin()方法中，
  // 具体怎么计算的可以看在measureChild()、measureChildren()、measureChildWidthMargin()中调用的getChildMeasureSpec()方法）；
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);//调用这个父方法，能够实现设置ViewGroup自身所需的尺寸。

    // 1.方法中的参数怎么来的-----由本View的父View中的layout_width，layout_height和padding以及本View的LayoutParams共同决定。
    // 参见ViewGroup的getChildMeasureSpec()方法（所以在本View的此方法中，无需解析本View的LayoutParas中的layout_height和layout_width属性）<P>

    // 2. 参数 widthMeasureSpec
    // 这个值由高32位和低16位组成，高32位保存的值叫specMode，可以通过如代码中所示的MeasureSpec.getMode()获取；
    // 低16位为specSize，同样可以由MeasureSpec.getSize()获取；

    // 需要注意的是：padding属性是自身属性，也包含在specSize中，所以specSize减去padding才是显示内容的尺寸；margin不是自身尺寸的范畴，
    // 所以specSize是已经减去了自身margin后的值。

    // 注意：specMode是本身的LayoutParams指定的（例如，layout_width="wrap_content"）；
    // specSize则是父布局和本身LayoutParams共同决定的（由android系统来生成）。

    // specMode-----
    // MeasureSpec.EXACTLY：父视图希望子视图的大小应该是specSize中指定的。(一般对应与
    // ViewGroup.LayoutParams.MATCH_PARENT,或者直接指定大小)
    // MeasureSpec.AT_MOST：子视图的大小最多是specSize中指定的值，也就是说不建议子视图的大小超过specSize中给定的值。（一般对应与
    // ViewGroup.LayoutParams.WRAP_CONTENT）
    // MeasureSpec.UNSPECIFIED：我们可以随意指定视图的大小。（这个暂时不知道是怎么出来的，有一个猜测，RecyclerView,ListView等列表ViewGroup会给子View传递这个值）<P>
    // 从这里可以看出，这个方法就是用来根据布局属性来确定 自身尺寸的。

    // 3.必须在这个方法中调用
    // setMeasuredDimension()方法来确定自身尺寸。如果是ViewGroup还必须还要遍历调用子View（View或者ViewGroup）的
    // measureChildWithMargins()方法(其实measureChildWithMargins()方法中只是简单的调用了View.measure()方法，View.measure()
    // 方法会触发调用View的onMeasure()方法)。

    // 4.其实实现是非常简单的，如果只是测量子View的尺寸直接调用measureChildWithMargins()方法即可，如果有特殊需求的话就需要来
    // 自己制定测量子View的规则了。
  }

  // 自定义View(非ViewGroup)不需要重写这个方法<P>
  // 自定义ViewGroup 必须重写这个方法<P>
  // 这个方法是确定怎么放置**子View**的，其实ViewGroup根据这个方法来确定把子View画到画布的什么位置上。
  //（具体逻辑应该是这样的，一个画布对应一个Activity，ViewGroup负责确定子View在画布上的位置而View只专注负责绘制具体内容）。<P>
  // 关于 本ViewGroup 如何放置（布局），是 本ViewGroup 的父View来确定的。<P>
  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    // 1.ViewGroup在这个方法中遍历调用子View的layout()方法来实现布局子View。<P>
    // 2.子View通过调用setFrame()方法用来设置自身相对于父容器（本ViewGroup）的位置的。但是这个方法是一个隐藏方法！
    // 因为在View.layout()方法中系统会自动调用这个方法。<P>
  }

  // 一般只有自定义View（非ViewGroup）重写这个方法，如果不需要根据布局属性来计算自身尺寸的话，简单重写这个方法直接用系统确定的
  // 尺寸就可以了。<p>
  // ViewGroup一般不重写这个方法,而是重写onMeasure()方法来计算自身尺寸，因为ViewGroup需要在onMeasure()
  // 中遍历调用子View的measureChildWithMargins()方法来把布局属性传递给子View以便子View计算自身尺寸。<P>
  // 当Android确定了View的大小后，会回调这个方法，通知你View的确切尺寸。所以这个方法一般在onMeasure()方法之后调用，
  // 有时候也不调用（View的尺寸没有变化时不调用）<P>
  // 这个方法传递进来的参数是View的确切尺寸，用来计算onDraw()方法需要绘制的尺寸。<p>
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    // 1.View(非ViewGroup)如果不是特别关心布局属性的话直接拿 w和h（都包含着padding值）来确定绘制尺寸就可以了。
  }

  // 一般只有自定义View（非ViewGroup）的时候重写
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
  }

  //一次touch事件的起点是ACTION_DOWN，终点是ACTION_UP或者ACTION_CANCEL。
  //
  //在 ACTION_DOWN 事件返回true表示仍然需要后续touch事件。
  //
  //View只有在 ACTION_DOWN 时返回了true，才有机会接收到后续TOUCH事件。例如，本View在此方法中ACTION_DOWN返回了true，在自己没有接收到
  // ACTION_CANCEL之前即使 ACTION_MOVE 返回false，它仍然可以一直接收到touch事件；但是一旦ACTION_DOWN返回了false，
  // 那么不在有机会接收到后续的touch事件了。
  //
  //负责分发touch事件，包括自己以及自己的子View，从自己的节点开始，所有touch事件都由此方法负责分发。
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    return super.dispatchTouchEvent(ev);
  }

  //负责拦截touch事件，一旦拦截到了（在某个touch事件ACTION_MOVE等返回true），就会转而执行自己的onTouchEvent()方法，且后续不会在执行此方法。
  //一旦拦截了事件，并且不是 ACTION_DOWN 事件，dispatchTouchEvent()方法会向 自己的子View 分发 ACTION_CANCEL 事件。
  //如果自己是最底层View，那么自己的 onInterceptTouchEvent() 方法不会执行？这个需要验证
  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    return super.onInterceptTouchEvent(ev);
  }

  //touch事件是从上向下传递 dispatchTouchEvent() -> onInterceptTouchEvent() -> onTouchEvent()；
  //一旦某层开始消费，就会阻隔再向下传递。
  //
  //处理自己接收到的Touch事件。
  //返回true表示消耗事件，false表示不消耗，注意它的ACTION_DOWN返回值比较重要，决定了它是否要这次touch事件，
  // 如果它在ACTION_DOWN返回了true，其他action类型返回false，不回影响它后续touch事件的接收，唯一影响的是它所在的Activity的onTouchEvent()是否执行。
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return super.onTouchEvent(event);
  }
}
