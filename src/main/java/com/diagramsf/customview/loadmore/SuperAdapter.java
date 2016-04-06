package com.diagramsf.customview.loadmore;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LayoutManager;
import com.tonicartos.superslim.LinearSLM;

/**
 * 与{@link SuperRecyclerView}配合使用实现上拉加载效果
 *
 * @author chenchong
 *         15/8/19
 *         上午12:08
 */
public class SuperAdapter<Adapter extends RecyclerView.Adapter> extends RecyclerView.Adapter {
    protected static final int INVALIDATE_POSITION = -1;
    private Adapter adapter;
    private boolean enable;
    private boolean isFailed;
    private boolean isFinal;

    private boolean showSection;//是否适配Section对应的LayoutManager
    private int firstSectionPosition;

    private LoadMoreFooterView mIFooterView;
    private View mFooterView;

    /** footerView接口 */
    public interface LoadMoreFooterView {
        View onCreateFooterView(Context context, ViewGroup parent);

        void onLoadMoreComplete(Context context);

        void onLoadMoreNothing(Context context);

        void onLoadMoreFailed(Context context);

        void onLoadMoreNormal(Context context);
    }

    public SuperAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    /**
     * @param firstSectionPosition 配合{@link LayoutManager}使用实现卡顶Section功能，这个参数见
     *                             {@link LayoutManager.LayoutParams#setFirstPosition(int)}来给footerView设置所属的Setion
     *                             的Position
     */
    public SuperAdapter(Adapter adapter, int firstSectionPosition) {
        this.adapter = adapter;
        this.showSection = true;
        this.firstSectionPosition = firstSectionPosition;
    }

    /** 必须在{@link RecyclerView#setAdapter(RecyclerView.Adapter)} 之前调用 */
    public void initLoadMoreFooterView(LoadMoreFooterView footerView) {
        mIFooterView = footerView;
    }

    public void updateFirstSectionPosition(int firstSectionPosition) {
        this.firstSectionPosition = firstSectionPosition;
        notifyDataSetChanged();
    }

    public Adapter getAdapter() {
        return adapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == INVALIDATE_POSITION && enable) {
            return new SuperViewHolder(mIFooterView.onCreateFooterView(parent.getContext(),parent));
        }
        return adapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == INVALIDATE_POSITION) {
            //noinspection unchecked
            Context context = holder.itemView.getContext();
            if (isFailed) {
                mIFooterView.onLoadMoreFailed(context);
            } else if (isFinal) {
                mIFooterView.onLoadMoreNothing(context);
            } else {
                mIFooterView.onLoadMoreNormal(context);
            }

            if (showSection) {
                final LayoutManager.LayoutParams params = GridSLM.LayoutParams.from(holder.itemView.getLayoutParams());
                params.setSlm(LinearSLM.ID);
                params.setFirstPosition(firstSectionPosition);
                holder.itemView.setLayoutParams(params);
            }
        } else {
            //noinspection unchecked
            adapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        if (enable && adapter.getItemCount() > 0) {
            return adapter.getItemCount() + 1;
        } else {
            return adapter.getItemCount();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == adapter.getItemCount() && enable) {
            return INVALIDATE_POSITION;
        } else {
            return adapter.getItemViewType(position);
        }
    }

    private class SuperViewHolder extends RecyclerView.ViewHolder {
        public SuperViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected void setLoadMoreEnable(boolean enable) {
        this.enable = enable;
        notifyDataSetChanged();
    }

    protected boolean isLoadMoreEnable() {
        return enable;
    }

    protected void loadMoreComplete() {
        this.enable = true;
        this.isFailed = false;
        this.isFinal = false;
        notifyDataSetChanged();
    }

    protected void loadMoreFailed() {
        this.enable = true;
        this.isFailed = true;
        this.isFinal = false;
        notifyDataSetChanged();
    }

    protected void loadMoreNothing() {
        this.enable = true;
        this.isFailed = false;
        this.isFinal = true;
        notifyDataSetChanged();
    }

    protected void loadMoreNormal() {
        this.enable = true;
        this.isFailed = false;
        this.isFinal = false;
        notifyDataSetChanged();
    }

    protected boolean isFinal() {
        return this.isFinal;
    }
}