package com.diagramsf.customview.loadmore;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * 可上拉加载的{@link RecyclerView}
 *
 * @author chenchong
 *         15/8/18
 *         下午10:02
 */
public class SuperRecyclerView extends RecyclerView {
    private Listener listener;
    private boolean enable;
    private boolean loading;

    public SuperRecyclerView(Context context) {
        super(context);
    }

    public SuperRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SuperRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == SCROLL_STATE_IDLE// 状态为静止
                && listener != null// 回调不为空
                && isLoadMoreEnable()// 上拉加载可用
                && !isFinal()// 不是最后一页
                && isValidPositionToLoadMore()
                && !loading)// 当前item是RecyclerView的最后一个item
        {
            loading = true;
            listener.onLoad();
            Adapter adapter = getAdapter();
            if (adapter instanceof SuperAdapter) {
                ((SuperAdapter) adapter).loadMoreNormal();
            }
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter instanceof SuperAdapter) {
            ((SuperAdapter) adapter).setLoadMoreEnable(enable);
        }
    }

    public Adapter getWrappedAdapter() {
        Adapter adapter = super.getAdapter();
        if (adapter instanceof SuperAdapter) {
            return ((SuperAdapter) adapter).getAdapter();
        } else {
            return adapter;
        }
    }


    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** 上拉加载完成,也就是正常状态 */
    public void setComplete() {
        Adapter adapter = super.getAdapter();
        if (adapter instanceof SuperAdapter) {
            ((SuperAdapter) adapter).loadMoreComplete();
        }
        loading = false;
    }

    /** 上拉加载失败 */
    public void setFailed() {
        setComplete();
        // 上拉加载失败
        Adapter adapter = super.getAdapter();
        if (adapter instanceof SuperAdapter) {
            ((SuperAdapter) adapter).loadMoreFailed();
        }
    }

    /** 上拉加载 没有更多了 */
    public void setFinal() {
        setComplete();
        Adapter adapter = super.getAdapter();
        if (adapter instanceof SuperAdapter) {
            ((SuperAdapter) adapter).loadMoreNothing();
        }
    }

    public void setLoadMoreEnable(boolean enable) {
        this.enable = enable;
        Adapter adapter = super.getAdapter();
        if (adapter instanceof SuperAdapter) {
            ((SuperAdapter) adapter).setLoadMoreEnable(enable);
        }
        loading = false;
    }

    public boolean isLoadMoreEnable() {
        return super.getAdapter() instanceof SuperAdapter && ((SuperAdapter) super.getAdapter()).isLoadMoreEnable();
    }

    public boolean isFinal() {
        return super.getAdapter() instanceof SuperAdapter && ((SuperAdapter) super.getAdapter()).isFinal();
    }


    private boolean isValidPositionToLoadMore() {
        boolean valid = false;
        final LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager
                && ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition() == layoutManager.getItemCount() - 1) {
            valid = true;
        } else if (layoutManager instanceof com.tonicartos.superslim.LayoutManager) {
            if (((com.tonicartos.superslim.LayoutManager) layoutManager).findLastVisibleItemPosition() == layoutManager.getItemCount() - 1) {
                valid = true;
            }
        }
        return valid;
    }

    public interface Listener {
        void onLoad();
    }
}
