package com.diagramsf.netvolley.netrepository;

import com.android.volley.VolleyError;
import com.diagramsf.helpers.StringUtils;
import com.diagramsf.net.NetRequest;
import com.diagramsf.net.comm.NetFailResultComm;
import com.diagramsf.netvolley.NetResultFactory;
import com.google.common.base.Preconditions;

/**
 * 根据本地保存的接口版本号，来请求数据。
 * </p>
 * 如果服务器版本号与本地版本号不同，就会请求服务器的数据，如果相同请求本地数据；
 * 要是本地数据不合法会删除本地版本号再请求服务器数据
 * </P>
 * 只有 {@link NetRequest.NetSuccessResult#checkResultLegitimacy()} 合法，
 * 才会调用{@link NetResultCallback#onSucceed(NetSuccessResult)}方法传递结果
 */
public class VersionRequest implements NetRequest {

    private static final int Recursion_MAX = 4;// 最多递归的次数，防止无线循环

    private String mUrl;
    private String mPostData;
    private final NetResultFactory mFactory;
    private final String mCancelTag;

    private String mCacheKey;

    /**
     * 请求 方式，0表示 根据报头信息缓存字段请求；1表示优先缓存（不根据报头缓存字段）；2 跳过缓存，直接请求网络（会保存请求结果）；3 无缓存请求；
     */
    private int mRequestType = 1;

    private NetRequest.NetSuccessResult mCacheLegitimacyResultData = null;// 缓存请求的合法结果

    private int mRecursionCounts = 0;// 检测到数据不合法，递归请求的次数

    private boolean mIsFirstRequest = false;// 是否是第一次请求

    private Object mDeliverToResultTag;

    private VersionCallback mVersionCallback;

    interface VersionCallback extends NetResultCallback {
        /**
         * 重置本地版本号为原始值
         *
         * @return 重置完版本号后，重新返回 postParam(或者url--如果发起的请求时 没有post参数，那么这里就是返回url)
         */
        String onUpdateLocalVersion();
    }

    /**
     * @param url       接口连接
     * @param postData  post传递的参数
     * @param cancelTag 取消请求的标识
     * @param factory   数据结果生成的工厂
     * @param cacheKey  缓存键值，这个值最好是URL+postData 但是一定要去掉版本号这个属性
     */
    public VersionRequest(String url, String postData, String cancelTag,
                          NetResultFactory factory, String cacheKey) {
        mUrl = url;
        mPostData = postData;
        mCancelTag = cancelTag;
        mFactory = factory;
        mCacheKey = cacheKey;
        mRequestType = 1;
    }

    @Override
    public void doRequest(@Type int type, Object cancelTag) {
        mIsFirstRequest = true;
        internalDoRequest(true);
    }

    public void setDeliverToResultTag(Object deliveredTag) {
        this.mDeliverToResultTag = deliveredTag;
    }

    /** 此处必须传递{@link VersionCallback} */
    @Override
    public void setResultCallBack(NetResultCallback callback) {
        Preconditions.checkArgument(callbackIsVersionCallback(callback));
        mVersionCallback = (VersionCallback) callback;
    }

    @Override
    public void setCacheKey(String cacheKey) {

    }

    @Override
    public void cancelRequest(Object cancelTag) {

    }

    NetResultCallback mCallback = new NetResultCallback() {
        @Override
        public void onSucceed(NetRequest.NetSuccessResult result) {
            NetRequest.NetSuccessResult.ResultType resultType = result.getResultType();
            boolean isLegitimacy = result.checkResultLegitimacy();
            if (resultType == NetRequest.NetSuccessResult.ResultType.CATCH) {// 来自缓存
                mRequestType = 2;
                if (isLegitimacy) {// 数据合法
                    // 保存起来，当验证完服务器结果后，确定是否将这个结果传递出去
                    mCacheLegitimacyResultData = result;
                }
                mIsFirstRequest = false;
                internalDoRequest(false);
            } else if (resultType == NetRequest.NetSuccessResult.ResultType.NET) { // 来自网络
                if (mRequestType == 1) {// 证明本地没有缓冲第一次请求结果就来自网络
                    if (isLegitimacy) {
                        mVersionCallback.onSucceed(result);
                    } else {
                        if (mRecursionCounts >= Recursion_MAX) {
                            NetFailResultComm failedResult = new NetFailResultComm(new VolleyError("请求递归次数超过限制！"));
                            mVersionCallback.onFailed(failedResult);
                            return;
                        }
                        mRecursionCounts++;
                        if (StringUtils.isEmpty(mPostData)) {
                            mUrl = mVersionCallback.onUpdateLocalVersion();
                        } else {
                            mPostData = mVersionCallback.onUpdateLocalVersion();
                        }
                        mIsFirstRequest = false;
                        internalDoRequest(false);
                    }
                } else if (mRequestType == 2) {// 证明第一次请求来自缓存，之后再次请求网络得到结果后执行这个分支
                    if (!isLegitimacy) {// 数据不合法,证明服务器端没有更新数据
                        if (null != mCacheLegitimacyResultData) {
                            // 将缓存的结果传递出去
                            mVersionCallback.onSucceed(mCacheLegitimacyResultData);
                        } else {
                            if (mRecursionCounts >= Recursion_MAX) {
                                NetFailResultComm failedResult = new NetFailResultComm(new VolleyError("请求递归次数超过限制！"));
                                mVersionCallback.onFailed(failedResult);
                                return;
                            }
                            mRecursionCounts++;
                            if (StringUtils.isEmpty(mPostData)) {
                                mUrl = mVersionCallback.onUpdateLocalVersion();
                            } else {
                                mPostData = mVersionCallback.onUpdateLocalVersion();
                            }
                            mRequestType = 2;
                            mIsFirstRequest = false;
                            internalDoRequest(false);
                        }
                    } else { // 证明服务器有更新的数据了
                        // 将网络请求的数据传递出去
                        mVersionCallback.onSucceed(result);
                    }
                }// resultType == ResultType.NET & mRequestType == 2 end

            }// resultType == ResultType.NET end
        }

        @Override
        public void onFailed(NetFailResult failResult) {
            if (null != mCacheLegitimacyResultData) {
                mVersionCallback.onSucceed(mCacheLegitimacyResultData);
            } else {
                if (mIsFirstRequest) { // 证明首次读取本地缓存的结果，出现异常，重新请求网络
                    mRequestType = 2;
                    mIsFirstRequest = false;
                    internalDoRequest(false);
                } else {
                    mVersionCallback.onFailed(failResult);
                }
            }
        }
    };// class end

    private void internalDoRequest(boolean clear) {
        if (clear) {
            mRecursionCounts = 0;
        }
        NetRequest request;
        switch (mRequestType) {
            case 0:
                break;
            case 1:
                request = new NetRequestImpl(mUrl, mPostData, mFactory);
                request.setDeliverToResultTag(mDeliverToResultTag);
                request.setCacheKey(mCacheKey);
                request.setResultCallBack(mCallback);
                request.doRequest(NetRequest.PRIORITY_CACHE, mCancelTag);
                break;
            case 2:
                request = new NetRequestImpl(mUrl, mPostData, mFactory);
                request.setDeliverToResultTag(mDeliverToResultTag);
                request.setCacheKey(mCacheKey);
                request.setResultCallBack(mCallback);
                request.doRequest(NetRequest.ONLY_NET_THEN_CACHE, mCancelTag);
                break;
            case 3:
                break;
        }
    }

    private boolean callbackIsVersionCallback(NetResultCallback callback) {
        return null != callback && callback instanceof VersionCallback;
    }
}
