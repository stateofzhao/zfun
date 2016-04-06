package com.diagramsf.net;

/** 网络请求成功请求结果 */
public interface NetResult {

    enum ResultType {
        /** 来自网络 */
        NET,
        /** 来自缓存 */
        CATCH,
        /**
         * 中间结果，首先取得缓存结果，再请求网络来获取结果
         */
        INTERMEDIATE
    }

    /** 设置数据来源 */
    void setResultType(ResultType resultType);

    /** 获得数据来源类型 */
    ResultType getResultType();

    /** 设置tag ,通过 {@link #getRequestDeliveredTag()}获取此值 */
    void setRequestDeliveredTag(Object tag);

    /** 获取相应的 {@link NetRequest#setDeliverToResultTag(Object tag)} 中设置的 tag */
    Object getRequestDeliveredTag();

    /**
     * 检测结果数据的合法性
     *
     * @return true 数据合法；false数据不合法
     */
    boolean checkResultLegitimacy();

    /**
     * 更新该结果类对应的 本地保存的数据,
     * <p>
     * 例如： 在{@link AbsFragmentListViewRefreshAndLoadmore}
     * 中加载数据完成后，检测到数据不合法，会调用这个方法后再重新执行请求，那么就需要在这个方法中重置本地保存的对应的版本号为0
     *
     *
     * @return 返回请求的参数(如果发起的请求时 没有post参数，那么这里就是返回url)
     */
    // public String updateLocalData();

}
