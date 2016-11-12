package com.diagramsf.learn;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义View 需要注意的重写方法！<P>
 *
 * 自定义View或者ViewGroup都不需要重写 measure()和layout()方法！因为这两个方法由Android系统接管实现。
 * <P>
 * 总结来说，自定义View(非ViewGroup)一般实现
 * onSizeChanged(),onDraw()即可，如果你需要更好地控制你的视图的布局参数需要实现onMeasure()方法； 自定义ViewGroup
 * 需要实现 onMeasure()和 onLayout()即可。
 */
public class LearnCustomView extends View {

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
  @Override protected int getSuggestedMinimumHeight() {
    return super.getSuggestedMinimumHeight();
  }

  //这个方法获取建议的最小宽度
  @Override protected int getSuggestedMinimumWidth() {
    return super.getSuggestedMinimumWidth();
  }

  // 自定义View（非ViewGroup）如果不关心 layout
  // parameters,可以不重写这个方法，只是重写onSizeChanged()方法即可。<P>
  // 自定义ViewGroup 需要重写这个方法。<P>

  // 这个方法就是根据布局属性（父容器的布局属性[通过两个参数传递进来]和自身布局属性[通过getLayoutParams()方法来获得]共同决定）来确定自身尺寸和通知View来计算其尺寸。
  // 如果是ViewGroup,如果layoutParams是wrap_content需要先计算子View的尺寸(measureChildWithMargins()来计算子View尺寸，然后通过子View的getMeasureWidth()等方法来后去子View的尺寸)，
  // 然后再来计算自己的尺寸。
  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);//调用这个父方法，能够实现设置ViewGroup自身所需的尺寸。

    // 1.方法中的参数怎么来的-----由父View中的layout_width，layout_height和padding以及View自身的layout_margin共同决定。
    // 权值weight也是尤其需要考虑的因素，有它的存在情况可能会稍微复杂点。参见ViewGroup的measureChildWithMargins()方法<P>

    // 2. 参数 widthMeasureSpec
    // 这个值由高32位和低16位组成，高32位保存的值叫specMode，可以通过如代码中所示的MeasureSpec.getMode()获取；
    // 低16位为specSize，同样可以由MeasureSpec.getSize()获取；
    // 需要注意的是 padding属性是自身属性，所以也包含在specSize中。所以在计算时要考虑到 内容的宽高需要减去padding值；不包括marging。
    // 注意：specMode是本身的LayoutParams指定的（例如，layout_width="wrap_content"）；
    // specSize则是父布局和本身LayoutParams共同决定的（由android系统来生成）。

    // specMode-----
    // MeasureSpec.EXACTLY：父视图希望子视图的大小应该是specSize中指定的。(一般对应与
    // ViewGroup.LayoutParams.MATCH_PARENT,或者直接指定大小)
    // MeasureSpec.AT_MOST：子视图的大小最多是specSize中指定的值，也就是说不建议子视图的大小超过specSize中给定的值。（一般对应与
    // ViewGroup.LayoutParams.WRAP_CONTENT）
    // MeasureSpec.UNSPECIFIED：我们可以随意指定视图的大小。（这个暂时不知道是怎么出来的）<P>
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
  // 关于本ViewGroup如何放置（布局）是本ViewGroup的父View来确定的。<P>
  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

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
  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    // 1.View(非ViewGroup)如果不是特别关心布局属性的话直接拿 w和h（都包含着padding值）来确定绘制尺寸就可以了。
  }

  // 一般只有自定义View（非ViewGroup）的时候重写
  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
  }
}
