package com.zfun.sharelib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 高度跟内容一样大的GridView，弊端itemview不会复用，只适合scrollview之类的嵌套使用
 */
public class InfinityGridView extends GridView {

    public InfinityGridView(Context context, AttributeSet attrs,int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InfinityGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InfinityGridView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
