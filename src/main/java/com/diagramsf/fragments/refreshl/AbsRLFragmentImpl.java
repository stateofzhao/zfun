package com.diagramsf.fragments.refreshl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.diagramsf.BasePresenter;
import com.diagramsf.customview.pullrefresh.PullRefreshLayout;
import com.diagramsf.net.NetRequest;
import com.diagramsf.netvolley.NetResultFactory;
import com.diagramsf.netvolley.loadmore.LoadmoreContract;
import com.diagramsf.netvolley.loadmore.LoadmorePresenter;
import com.diagramsf.netvolley.refresh.RefreshContract;
import com.diagramsf.netvolley.refresh.RefreshPresenter;

/**
 * 下拉刷新和上拉加载更多的模板Fragment,下拉刷新控件是{@link IPullRefreshView} 上拉加载更多控件
 * 是{@link IPullLoadMoreView} ，(如果提取出来只需要更改一下继承就行)。
 * <p/>
 * 注意事项：
 * <p/>
 * 1.必须在onCreateView()方法中调用 {@link #initView(IPullRefreshView, IPullLoadMoreView)}方法
 * <p/>
 * 2.必须手动调用 {@link #doFirstLoadData()}方法才能开启数据加载
 * <p/>
 * 3. 继承此类，重写抽象方法，即可！
 * <p/>
 *
 * @version 1.0
 *          Created by Diagrams on 2015/11/9 18:28
 */
public abstract class AbsRLFragmentImpl implements AbsRLFragmentI, RefreshContract.View, LoadmoreContract.View {

    public final static int LIMIT = 10;

    private final static int START_OFFSET = 0;
    private final static int INVALID_LIMIT = -1;

    private final static String ARG_ENABLE_REFRESH = "_enable_refresh_AbsLoadMore";
    private final static String ARG_ENABLE_LOAD_MORE = "_enable_loadMore_AbsLoadMore";
    private final static String ARG_LIMIT = "_limit_AbsLoadMore";
    private final static String ARG_START_OFFSET = "_startOffset_AbsLoadMore";
    private final static String ARG_OFFSET = "_offset_AbsLoadMore";

    private RefreshPresenter mRefreshPresenter;
    private LoadmorePresenter mLoadMorePresenter;

    private String mCancelTag;

    private int mLimit = LIMIT;
    private int mStartOffset = START_OFFSET;
    private int mOffset = START_OFFSET;

    private boolean mHashInit = false;//是否调用了initView()方法
    private boolean mEnableRefresh = true;//是否可以下拉刷新
    private boolean mEnableLoadMore = true;//是否可以上拉加载更多

    //下拉刷新View接口
    protected IPullRefreshView mRefreshView;
    //上拉加载更多View接口
    protected IPullLoadMoreView mLoadMoreView;

    private OnLoadMoreListener mLoadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            AbsRLFragmentImpl.this.onLoadMore();
        }
    };
    private OnRefreshListener mRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            AbsRLFragmentImpl.this.onRefresh();
        }

        @Override
        public void onRefreshComplete() {
            AbsRLFragmentImpl.this.onRefreshComplete();
        }
    };

    /** 上拉加载更多View接口 */
    public interface IPullLoadMoreView {
        /** 设置 上拉加载更多的监听器 */
        void setOnLoadMoreListener(OnLoadMoreListener listener);

        /** 是否能够上拉加载更多 */
        void setLoadMoreEnable(boolean enable);

        /** 上拉加载更多 正常状态 */
        void loadMoreNormal();

        /** 上拉加载更多 失败状态 */
        void loadMoreFailed();

        /** 上拉加载更多 没有更多了 */
        void loadMoreNothing();

        /** 执行上拉加载更多完成 */
        void loadMoreComplete();

        /** 销毁View */
        void destroy();
    }

    /** 下拉刷新View接口 */
    public interface IPullRefreshView {

        /** 设置上拉刷新的监听器 */
        void setOnRefreshListener(OnRefreshListener listener);

        /** 执行自动下拉刷新 */
        void autoRefresh();

        /** 停止刷新状态 */
        void stopRefresh();

        /** 销毁View */
        void destroy();

        /** 是否能够下拉刷新 */
        void setEnableRefresh(boolean enableRefresh);
    }

    public interface OnLoadMoreListener {
        /** 触发 上拉加载更多 回调事件 */
        void onLoadMore();
    }

    public interface OnRefreshListener {
        /** 触发刷新操作的回调事件 */
        void onRefresh();

        /** 刷新状态恢复到正常状态 */
        void onRefreshComplete();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mRefreshPresenter = new RefreshPresenter(this);
        mLoadMorePresenter = new LoadmorePresenter(this);

        if (null != savedInstanceState) {//恢复之前保存的数据
            mEnableRefresh = savedInstanceState.getBoolean(ARG_ENABLE_REFRESH);
            mEnableLoadMore = savedInstanceState.getBoolean(ARG_ENABLE_LOAD_MORE);
            mLimit = savedInstanceState.getInt(ARG_LIMIT);
            mStartOffset = savedInstanceState.getInt(ARG_START_OFFSET);
            mOffset = savedInstanceState.getInt(ARG_OFFSET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (!mHashInit) {
            throw new RuntimeException("must call initView() method at onCreateView() method!");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ARG_ENABLE_REFRESH, mEnableRefresh);
        outState.putBoolean(ARG_ENABLE_LOAD_MORE, mEnableLoadMore);
        outState.putInt(ARG_LIMIT, mLimit);
        outState.putInt(ARG_START_OFFSET, mStartOffset);
        outState.putInt(ARG_OFFSET, mOffset);
    }

    @Override
    public void onDestroyView() {
        //取消数据加载
        mRefreshPresenter.cancelRequest(mCancelTag);
        mLoadMorePresenter.cancelRequest(mCancelTag);

        //取消View资源占用
        if (null != mRefreshView) {
            mRefreshView.destroy();
            mRefreshView = null;
        }
        if (null != mLoadMoreView) {
            mLoadMoreView.destroy();
            mLoadMoreView = null;
        }
    }

    /**
     * 外部调用执行自动下拉刷新，如果首次加载缓存还没有执行完时，是不会执行自动下拉刷新的
     *
     * @return true调用成功；false调用失败
     */
    public boolean autoRefresh() {
        if (null == mRefreshView) {//没有初始化 PullRefreshLayout或者已经调用了onDestroyView()
            return false;
        }
        mRefreshView.autoRefresh();
        return true;
    }

    /**
     * 外部调用停止下拉刷新状态，同时也会取消刷新数据的请求
     *
     * @return true调用成功；false调用失败
     */
    public boolean stopRefresh() {
        if (null == mRefreshView) {//没有初始化 PullRefreshLayout或者已经调用了onDestroyView()
            return false;
        }
        mRefreshPresenter.cancelRequest(mCancelTag);
        mRefreshView.stopRefresh();
        return true;
    }

    /**
     * 首次加载数据
     */
    public final void doFirstLoadData() {
        final String url = onCreateFirstURL(mStartOffset, mLimit);
        final String postData = onCreateFirstPostData(mStartOffset, mLimit);
        final NetResultFactory factory = onCreateFirstJSONObjectResultFactory();

        mRefreshPresenter.firstLoadData(true, url, postData, mCancelTag, factory);
    }

    /** 内部自动下拉刷新 */
    protected void doAutoRefresh() {
        if (null == mRefreshView) {
            return;
        }
        mRefreshView.autoRefresh();
    }

    /**
     * 内部调用的 停止刷新状态
     */
    protected void doStopRefresh() {
        if (null == mRefreshView) {
            return;
        }
        mRefreshView.stopRefresh();
    }


    /**
     * 在{@link android.support.v4.app.Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)} 方法中调用
     *
     * @param refreshView  {@link IPullRefreshView}
     * @param loadMoreView {@link IPullLoadMoreView}
     */
    protected void initView(IPullRefreshView refreshView, IPullLoadMoreView loadMoreView) {
        initRefreshView(refreshView);
        mRefreshView = refreshView;
        mLoadMoreView = loadMoreView;
        if (null != mLoadMoreView) {
            mLoadMoreView.setLoadMoreEnable(mEnableLoadMore);
            mLoadMoreView.setOnLoadMoreListener(mLoadMoreListener);
        }
        buildCommRequestData();
        mHashInit = true;
    }

    /**
     * 是否可以下拉刷新
     *
     * @param enablePullRefresh true可以下拉刷新，false 不可以
     */
    final public void setEnablePullRefresh(boolean enablePullRefresh) {
        mEnableRefresh = enablePullRefresh;
        if (null == mRefreshView) {
            return;
        }
        mRefreshView.setEnableRefresh(enablePullRefresh);
    }

    /**
     * 是否可以上拉加载更多
     *
     * @param enableLoadMore true可以上拉加载更多，false 不可以
     */
    final public void setEnableLoadMore(boolean enableLoadMore) {
        mEnableLoadMore = enableLoadMore;
        if (null == mLoadMoreView) {
            return;
        }
        mLoadMoreView.setLoadMoreEnable(enableLoadMore);
    }

    private void initRefreshView(IPullRefreshView refreshView) {
        if (null != refreshView) {
            //设置是否 能够下拉刷新
            refreshView.setEnableRefresh(mEnableRefresh);
            refreshView.setOnRefreshListener(mRefreshListener);
        }
    }

    /** 构建通用的网络请求数据 */
    private void buildCommRequestData() {
        mCancelTag = onCreateCancelNetTAG();
        int limit = onCreateLimit();
        if (INVALID_LIMIT != limit) {
            mLimit = limit;
        }
        int startOffset = onCreateStartOffset();
        if (INVALID_LIMIT != startOffset) {
            mStartOffset = startOffset;
        }
    }

    public final void onLoadMore() {

        doStopRefresh();
        mRefreshPresenter.cancelRequest(mCancelTag);
        mLoadMorePresenter.cancelRequest(mCancelTag);

        final String url = onCreateLoadMoreURL(mOffset, mLimit);
        final String postData = onCreateLoadMorePostData(mOffset, mLimit);
        final NetResultFactory factory = onCreateLoadMoreJSONObjectResultFactory();
        mLoadMorePresenter.doLoadmore(url, postData, mCancelTag, factory);
    }

    public final void onRefresh() {
        onBeginRefresh();

        //取消之前的数据加载
        mRefreshPresenter.cancelRequest(mCancelTag);
        mLoadMorePresenter.cancelRequest(mCancelTag);

        //14869 XTV列表，应用在下拉刷新过程中退到后台再进入应用，小汽车一直在跑  lzf 2016/2/23 由于这个bug不能够再使用postDelayed了，否则会造成刷新是按Home键永远不会停止刷新，目前原因未找到
        //14640 专辑列表加载失败，小飞船页面，下拉刷新过程中，小飞船未渐隐消失  lzf  2016/1/29 start
        //        getMainThreadHandler().postDelayed(mdoRefreshTask, 1000);
        final String url = onCreateRefreshURL(mStartOffset, mLimit);
        final String postData = onCreateRefreshPostData(mStartOffset, mLimit);
        final NetResultFactory factory = onCreateRefreshJSONObjectResultFactory();
        mRefreshPresenter.doRefresh(url, postData, mCancelTag, factory);
        //14640 专辑列表加载失败，小飞船页面，下拉刷新过程中，小飞船未渐隐消失  lzf  2016/1/29 start

    }

    public final void onRefreshComplete() {

    }

    //===========================MVP中的View回调接口==============start
    @Override
    public void setPresenter(BasePresenter presenter) {

    }

    @Override
    public final void showLoadMoreProgress() {
        mLoadMoreView.loadMoreNormal();
    }

    @Override
    public final void hideLoadMoreProgress() {
        //此处不需要处理
    }

    @Override
    public void loadMoreFail(NetRequest.NetFailResult result) {
        //设置 加载更多状态
        mLoadMoreView.loadMoreFailed();
        mLoadMoreView.loadMoreComplete();
        //回调结果
        onLoadMoreFail(result);
    }

    @Override
    public void loadMoreFinish(NetRequest.NetSuccessResult result) {
        //更新页数
        mOffset += mLimit;
        mLoadMoreView.loadMoreComplete();
        //回调结果
        onLoadMoreResult(result);
    }


    @Override
    public void showFirstCacheRequestProgress() {
        mRefreshView.setEnableRefresh(false);//首次加载缓存期间禁止下拉刷新
        onShowFirstLoadCacheProgress();
    }

    @Override
    public void hideFirstCacheRequestProgress() {
        onHideFirstLoadCacheProgress();
        //恢复下拉刷新状态
        mRefreshView.setEnableRefresh(mEnableRefresh);
    }

    @Override
    public void showFirstNetRequestProgress() {
        mRefreshView.setEnableRefresh(false);//首次加载网络期间禁止下拉刷新
        onShowFirstLoadNetProgress();
    }

    @Override
    public void hideFirstNetRequestProgress() {
        onHideFirstLoadNetProgress();
        //恢复下拉刷新状态
        mRefreshView.setEnableRefresh(mEnableRefresh);
    }

    @Override
    public final void showFirstCacheResult(NetRequest.NetSuccessResult result) {
        onFirstCacheLoadResult(result);
        mOffset += mLimit;//更新页数
    }

    @Override
    public final void showFirstCacheFail(NetRequest.NetFailResult failResult) {
        onFirstCacheLoadFail(failResult);
    }

    @Override
    public final void showFirstNoCache() {
        onFirstCacheLoadNoCache();
    }

    @Override
    public void showFirstNetResult(NetRequest.NetSuccessResult result) {
        onFirstNetLoadResult(result);
    }

    @Override
    public void showFirstNetFail(NetRequest.NetFailResult failResult) {
        onFirstNetLoadFail(failResult);
    }


    @Override
    public void showRefreshResult(NetRequest.NetSuccessResult result) {
        //回调结果
        boolean childIntercept = onRefreshResult(result);
        //设置页数
        mOffset = mStartOffset + mLimit;
        //停止刷新状态
        if (!childIntercept) {
            doStopRefresh();
        }
    }

    @Override
    public void showRefreshFail(NetRequest.NetFailResult failResult) {
        //回调结果
        boolean childIntercept = onRefreshFail(failResult);
        //停止刷新状态
        if (!childIntercept) {
            doStopRefresh();
        }
    }//===========================MVP中的View回调接口==============end

    /** 分页，一页的数据个数限制 ,可以被重写，默认的是 10 */
    public int onCreateLimit() {
        return INVALID_LIMIT;
    }


    //-----------------------------下面的方法有特殊需求可以重写--------------------

    /** 分页，开始的页数，默认是0 */
    public int onCreateStartOffset() {
        return INVALID_LIMIT;
    }

    /** 首次进入页面时 请求网络数据 传递的post参数 */
    public String onCreateFirstPostData(int offset, int limit) {
        return null;
    }

    /** 下拉刷新 请求网络数据的 URL */
    public String onCreateRefreshURL(int offset, int limit) {
        return onCreateFirstURL(offset, limit);
    }

    /** 下拉刷新 请求网络数据的 PostData */
    public String onCreateRefreshPostData(int offset, int limit) {
        return onCreateFirstPostData(offset, limit);
    }

    /** 下拉刷新 请求网络数据返回结果 生成工厂 */
    public NetResultFactory onCreateRefreshJSONObjectResultFactory() {
        return onCreateFirstJSONObjectResultFactory();
    }

    /** 上拉加载更多，请求网络数据的 URL */
    public String onCreateLoadMoreURL(int offset, int limit) {
        return onCreateFirstURL(offset, limit);
    }

    /** 上拉加载更多, 请求网络数据的 PostData */
    public String onCreateLoadMorePostData(int offset, int limit) {
        return onCreateFirstPostData(offset, limit);
    }

    /** 上拉加载更多, 请求网络数据返回结果 生成工厂 */
    public NetResultFactory onCreateLoadMoreJSONObjectResultFactory() {
        return onCreateFirstJSONObjectResultFactory();
    }

    /** 开始执行刷新 */
    public void onBeginRefresh() {
        mLoadMoreView.loadMoreComplete();
    }

    /** 首次加载缓存数据失败(证明请求到了本地缓存，并且也读取成功了，但是解析缓存数据的时候挂掉了) */
    protected void onFirstCacheLoadFail(NetRequest.NetFailResult failResult) {
        onFirstCacheLoadFail();
    }

    /** 首次加载缓存数据，没有请求到缓存数据--本地没有缓存数据 */
    protected void onFirstCacheLoadNoCache() {
        onFirstCacheLoadFail();
    }

    /** 显示执行首次数据请求  之 请求缓存数据的进度条 */
    protected void onShowFirstLoadCacheProgress() {

    }

    /** 隐藏执行首次数据请求  之 请求缓存数据的进度条 */
    protected void onHideFirstLoadCacheProgress() {

    }

    /** 显示执行首次数据请求  之 请求网络数据的进度条 */
    protected void onShowFirstLoadNetProgress() {

    }

    /** 隐藏执行首次数据请求  之 请求网络数据的进度条 */
    protected void onHideFirstLoadNetProgress() {

    }

    //-------------------------------------下面都是抽象方法----------------------------------------

    /** 返回 一个唯一的字符串标识 */
    public abstract String onCreateSingleTAG();

    /** 生成取消网络加载 用的tag */
    public abstract String onCreateCancelNetTAG();

    /**
     * 首次进入页面时 请求数据的URL
     *
     * @param offset 当前页数
     */
    public abstract String onCreateFirstURL(int offset, int limit);

    /** 首次加载页面，生成结果对象的接口 */
    public abstract NetResultFactory onCreateFirstJSONObjectResultFactory();

    /**
     * 首次加载缓存数据成功
     */
    public abstract void onFirstCacheLoadResult(NetRequest.NetSuccessResult result);

    /** 首次加载缓存数据失败 */
    public abstract void onFirstCacheLoadFail();

    /**
     * 首次加载网络数据成功
     */
    public abstract void onFirstNetLoadResult(NetRequest.NetSuccessResult result);

    /**
     * 首次加载网络数据失败
     */
    public abstract void onFirstNetLoadFail(NetRequest.NetFailResult failResult);

    /**
     * 下拉刷新加载数据成功
     *
     * @return false表示这个方法执行完后让 {@link AbsRLFragmentImpl}直接调用{@link PullRefreshLayout#stopRefresh()},
     * true 表示不调用{@link PullRefreshLayout#stopRefresh()}，需要自己在合适的时机调用 {@link PullRefreshLayout#stopRefresh()}
     * 来结束刷新状态
     */
    public abstract boolean onRefreshResult(NetRequest.NetSuccessResult result);

    /**
     * 下拉刷新加载数据失败
     *
     * @return false表示这个方法执行完后让 {@link AbsRLFragmentImpl}直接调用{@link PullRefreshLayout#stopRefresh()},
     * true 表示不调用{@link PullRefreshLayout#stopRefresh()}，需要自己在合适的时机调用 {@link PullRefreshLayout#stopRefresh()}
     * 来结束刷新状态
     */
    public abstract boolean onRefreshFail(NetRequest.NetFailResult failResult);

    /** 上拉加载更多成功 */
    public abstract void onLoadMoreResult(NetRequest.NetSuccessResult result);

    /** 上拉加载更多失败 */
    public abstract void onLoadMoreFail(NetRequest.NetFailResult failResult);

}
