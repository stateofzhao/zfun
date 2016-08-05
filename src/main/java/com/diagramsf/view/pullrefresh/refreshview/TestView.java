package com.diagramsf.view.pullrefresh.refreshview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;
import com.diagramsf.view.pullrefresh.PullRefreshLayout;
import com.diagramsf.view.pullrefresh.RefreshHeader;

public class TestView extends TextView implements RefreshHeader {
	private StringBuilder sb = new StringBuilder();

	public TestView(Context context) {
		super(context);
	}

	public TestView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TestView(Context context, AttributeSet attrs,
                    int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	public int onCreateTrigRefreshHeight() {
		return getMeasuredHeight();
	}

	@Override
	public void onBeginRefresh() {
		sb.append("\n===beginRefresh===");
		setText(sb);
	}

	@Override
	public void onStopRefresh() {

	}

	@Override
	public void onStopRefreshComplete() {

	}

	@Override
	public void onContentViewScrollDistance(int distance, PullRefreshLayout.State state) {
		setText(sb + "contentViewScrollDistance---距离：" + distance);
	}

	@Override
	public void onContentViewBeginScroll() {
		sb.delete(0, sb.length());
		sb.append("===contentViewBeginScroll===\n");
		setText(sb);
	}

	@Override
	public void onContentViewEndScroll() {
		sb.append("\n===contentViewEndScroll===");
		setText(sb);
	}
}
