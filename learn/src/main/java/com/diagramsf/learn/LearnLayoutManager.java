package com.diagramsf.learn;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Diagrams on 2016/8/5 11:22
 */
public class LearnLayoutManager extends RecyclerView.LayoutManager{
  public LearnLayoutManager() {
    //setAutoMeasureEnabled(true);//让RecyclerView能够使滚动方向的 自身LayoutParams的 wrap_content 起作用
  }

  //初步理解：1.RecyclerView回调此方法来设置RecyclerView自身的尺寸。
  //2.一般不在此方法中来让子View测量自身尺寸，具体啥原因不知道。
  @Override
  public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec,
      int heightSpec) {
    super.onMeasure(recycler, state, widthSpec, heightSpec);
  }

  @Override public RecyclerView.LayoutParams generateDefaultLayoutParams() {
    return new LearnLayoutParams(
        LearnLayoutParams.WRAP_CONTENT,
        LearnLayoutParams.WRAP_CONTENT);
  }

  //初步理解：1.这个方法不是布局正在显示和预显示的View的，而是直接布局整个RecyclerView的Adapter中所有的View。
  //2.这个方法会在RecyclerView的onMeasure()方法中调用，所以应该在这里来测量子View的尺寸。
  @Override public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    //在布局之前，将所有的子View先Detach掉，放入到Scrap缓存中，主要是考虑到，
    // 屏幕上可能还有一些ItemView是继续要留在屏幕上的，我们不直接Remove，而是选择Detach
    detachAndScrapAttachedViews(recycler);

    final int itemCount = getItemCount();//获取所有子View个数
    for (int i = 0; i < itemCount; i++) {//遍历所有View，计算View的尺寸并布局View

      //首先去检查Scrap缓存是否有对应的position的View，如果有，则直接拿出来可以直接用，
      // 不用去重新绑定数据；如果没有，则从Recycle缓存中取，
      // 并且会回调Adapter的onBindViewHolder方法（当然了，如果Recycle缓存为空，
      // 还会调用onCreateViewHolder方法），最后再将绑定好新数据的View返回。
      View child = recycler.getViewForPosition(i);
      //将View添加到RecyclerView中
      addView(child);
      //对子View进行测量
      measureChildWithMargins(child,0,0);


    }
  }

  /** {@link LearnLayoutManager}需要的 布局属性 */
  public static final class LearnLayoutParams extends RecyclerView.LayoutParams {

    public LearnLayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
    }

    public LearnLayoutParams(ViewGroup.LayoutParams source) {
      super(source);
    }

    public LearnLayoutParams(RecyclerView.LayoutParams source) {
      super(source);
    }

    public LearnLayoutParams(ViewGroup.MarginLayoutParams source) {
      super(source);
    }

    public LearnLayoutParams(int width, int height) {
      super(width, height);
    }
  }
}
