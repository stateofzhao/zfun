package com.diagramsf.customview.pullrefresh.refreshview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;
import com.diagramsf.customview.pullrefresh.PullRefreshLayout;
import com.diagramsf.customview.pullrefresh.RefreshViewCallback;

public class TestRefreshView extends TextView implements RefreshViewCallback {
	public TestRefreshView(Context context) {
		super(context);
	}

	public TestRefreshView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TestRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TestRefreshView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	public int getRefreshHeight() {
		return getMeasuredHeight();
	}

	private StringBuilder sb = new StringBuilder();

	@Override
	public void doRefresh() {
		sb.append("\n===doRefresh===");
		setText(sb);
	}

	@Override
	public void refreshStop() {

	}

	@Override
	public void contentViewBeginScroll() {

		sb.delete(0, sb.length());
		sb.append("===contentViewBeginScroll===\n");

		setText(sb);

	}

	@Override
	public void contentViewEndScroll() {
		sb.append("\n===contentViewEndScroll===");
		setText(sb);

	}

	@Override
	public void contentViewScrollDistance(int distance,PullRefreshLayout.State state) {
		setText(sb + "contentViewScrollDistance---距离：" + distance);

	}
}
