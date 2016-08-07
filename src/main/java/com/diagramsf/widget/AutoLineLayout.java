package com.diagramsf.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.diagramsf.R;

import java.util.ArrayList;
import java.util.List;


/**
 * 横向排列自动换行的布局,1.0.0不支持纵向补齐功能
 * 
 * @author lzf
 * @version 1.0.0
 * */
public class AutoLineLayout extends ViewGroup {
	private static int DEFAULT_ITEM_SPACE = 10;
	private static int DEFAULT_LINE_SPACE = 10;

	private int mItem_space = DEFAULT_ITEM_SPACE;// 一行中项之间的间隔
	private int mLine_space = DEFAULT_LINE_SPACE; // 行之间的间隔

	private List<Integer> mLines = new ArrayList<Integer>();// 放置每行的item个数
	private List<Integer> mLineHeight = new ArrayList<Integer>();// 放置每行的高度

	public AutoLineLayout(Context context) {
		super(context);
	}

	public AutoLineLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public AutoLineLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public AutoLineLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	// 解析AutoLineLayout 的自定义属性
	private void init(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.AutoLineLayout);
		mItem_space = a.getDimensionPixelSize(
				R.styleable.AutoLineLayout_itemSpace, DEFAULT_ITEM_SPACE);
		mLine_space = a.getDimensionPixelSize(
				R.styleable.AutoLineLayout_lineSpace, DEFAULT_ITEM_SPACE);
		a.recycle();

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// System.out.println("执行 onMeasure()方法");
		mLines.clear();
		mLineHeight.clear();
		int maxHeight = 0;// 本布局最大高度
		int maxWidth = 0;// 本布局最大宽度
		int xpad = getPaddingLeft() + getPaddingRight();
		int ypad = getPaddingTop() + getPaddingBottom();

		// 计算AutoLineLayout宽度
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode == MeasureSpec.EXACTLY
				|| widthMode == MeasureSpec.AT_MOST) {
			// 由于是根据本布局的指定宽度来控制是否换行的，所以本布局的宽度是根据传递进来的参数得到的，不是根据子View的大小来计算的
			maxWidth = MeasureSpec.getSize(widthMeasureSpec);
		}

		int allLineHeights = 0; // 所有行的高度
		int pre_oneLineMaxHeight = 0;// 用来存储上一次行高
		int oneLineMaxHeight = 0; // 一行的最大高度

		int line_items_witdh = 0;// 一行itemView的宽度和间距的总和
		int line_items = 0; // 整行的itemView个数。（能够占满一行的所有itemView的个数）
		int itemView_noGone = 0;// itemView中不是GONE状态的个数
		int childCount = getChildCount(); // 子View个数
		for (int i = 0; i < childCount; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {// GONE的话忽略View的占位
				itemView_noGone++;
				// 测量子View的尺寸
				measureChildWithMargins(child, widthMeasureSpec, mItem_space,
						heightMeasureSpec, 0);
				final LayoutParams lp = (LayoutParams) child.getLayoutParams();

				int childMeasuredWidth = child.getMeasuredWidth();
				int childMeasuredHeight = child.getMeasuredHeight();
				int childMarginLeft = lp.leftMargin;
				int childMarginRight = lp.rightMargin;
				int childMarginTop = lp.topMargin;
				int childMarginBom = lp.bottomMargin;

				// System.out.println("AutoLineLayout 测量子View的宽度："
				// + childMeasuredWidth);
				// System.out.println("AutoLineLayout 测量子View的高度："
				// + childMeasuredHeight);

				if (childMeasuredWidth + childMarginLeft + childMarginRight > maxWidth
						- xpad) {// 子View的宽度大于了本View的宽度，抛出异常
					throw new IllegalStateException("子View的宽度不能大于AutoLineLayout的宽度");
				}

				if (0 == i) {
					line_items_witdh = childMeasuredWidth + childMarginLeft
							+ childMarginRight;

					oneLineMaxHeight = childMeasuredHeight + childMarginTop
							+ childMarginBom; // 第一行的高度
					pre_oneLineMaxHeight = oneLineMaxHeight;
				} else {
					// 不是每一行的开头itemView，必须加上设置的itemView之间的间隔
					line_items_witdh += mItem_space + childMeasuredWidth
							+ childMarginLeft + childMarginRight;

					if (childMeasuredHeight + childMarginTop + childMarginBom > oneLineMaxHeight) {
						pre_oneLineMaxHeight = oneLineMaxHeight;
						oneLineMaxHeight = childMeasuredHeight + childMarginTop
								+ childMarginBom;
					}
				}

				if (line_items_witdh > maxWidth - xpad) {// 该换行了,此时这个子View就是下一行开始的第一个View
					// 计算一行有多少个View
					int lineContainItems = 0;
					if (mLines.size() > 0) {
						lineContainItems = i - mLines.get(mLines.size() - 1);
					} else {
						lineContainItems = i;
					}

					mLines.add(lineContainItems);// 将这一行的itemView个数保存起来
					line_items += lineContainItems;

					line_items_witdh = childMeasuredWidth + childMarginLeft
							+ childMarginRight;// 将下一行开头的itemView宽度记录下来

					mLineHeight.add(pre_oneLineMaxHeight);// 将行高记录下来
					allLineHeights += pre_oneLineMaxHeight;// 累加行高
					oneLineMaxHeight = childMeasuredHeight + childMarginTop
							+ childMarginBom;// 重置行的最大高度

				}
			}
		}

		if (line_items < itemView_noGone) {
			// ---将非整行的最后一行的行数加上
			mLines.add(itemView_noGone - line_items);

			// ---将非整行的最后一行的行高加上
			mLineHeight.add(oneLineMaxHeight);// 将行高记录下来
			allLineHeights += oneLineMaxHeight;// 累加行高
		}

		// 将行间距加上
		allLineHeights += (mLines.size() - 1) * mLine_space;

		// 计算AutoLineLayout高度
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode == MeasureSpec.EXACTLY) {
			maxHeight = MeasureSpec.getSize(heightMeasureSpec);
			// System.out.println("AutoLineLayout 的高度： MeasureSpec.EXACTLY");
		} else if (heightMode == MeasureSpec.AT_MOST) {
			maxHeight = allLineHeights + ypad;
			// System.out.println("AutoLineLayout 的高度： MeasureSpec.AT_MOST");
		}else{
			maxHeight = allLineHeights + ypad;
		}

		// System.out.println("AutoLineLayout 的宽度：" + maxWidth);
		// System.out.println("AutoLineLayout 的高度：" + maxHeight);

		setMeasuredDimension(maxWidth, maxHeight);

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {

		int leftPos = getPaddingLeft(); // 子View距离左边的距离
		int topPos = getPaddingTop();// childView 最顶部的边框

		// for (int i = 0; i < mLines.size(); i++) {
		// System.out
		// .println("第  " + i + "  行有  " + mLines.get(i) + "  个View");
		// }

		int one_line_height;// 行高
		int hasLayoutViews = 0;// 已经布局了的View数

		Rect mTmpContainerRect = new Rect();// childView的布局空间,包括marging
		// Rect mTmpChildRect = new
		// Rect();//childView具体显示的位置，不包括marging，包括padding，padding需要子View自己计算的

		for (int i = 0; i < mLines.size(); i++) { // 根据有多少行来布局
			one_line_height = mLineHeight.get(i);
			int oneLineViewCount = mLines.get(i);// 取出指定行的view个数

			for (int j = 0; j < oneLineViewCount; j++) { // 将view布局到行中
				final View child = getChildAt(hasLayoutViews + j);

				if (child.getVisibility() != GONE) {
					final LayoutParams lp = (LayoutParams) child
							.getLayoutParams();
					final int width = child.getMeasuredWidth();
					final int height = child.getMeasuredHeight();

					mTmpContainerRect.left = leftPos + lp.leftMargin;
					mTmpContainerRect.right = leftPos + width + lp.rightMargin;
					leftPos = mTmpContainerRect.right + mItem_space;// 更新下个View距离左边的距离

					mTmpContainerRect.top = topPos + lp.topMargin;
					mTmpContainerRect.bottom = mTmpContainerRect.top + height
							+ lp.bottomMargin;

					// Place the child.
					child.layout(mTmpContainerRect.left, mTmpContainerRect.top,
							mTmpContainerRect.right, mTmpContainerRect.bottom);
				}

			}

			// -----换行了
			hasLayoutViews += oneLineViewCount;
			leftPos = getPaddingLeft();// 重置距离左边的距离
			topPos += one_line_height + mLine_space;// 更新下一行距离顶部的位置
		}

	}

	public void setItemSpace(int space_px) {
		this.mItem_space = space_px;
		requestLayout();
	}

	public void setLineSpace(int space_px) {
		this.mLine_space = space_px;
	}

	// 一下四个方法是给AutoLineLayout的子View定义LayoutParams---------------
	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new AutoLineLayout.LayoutParams(getContext(), attrs);
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(
			ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof AutoLineLayout.LayoutParams;
	}// --------------------------------------------------------------------

	/**
	 * AutoLineLayout的 LayoutParams,不是作用于 AutoLineLayout
	 * 自己的（不能给AutoLineLayout设置声明的属性），而是作用于其子View的！
	 * */
	public static class LayoutParams extends MarginLayoutParams {

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);

		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}

}
