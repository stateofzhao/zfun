package com.zfun.sharelib.init;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;

/**
 * Created by lzf on 2021/12/22 2:54 下午
 */
public interface IOptWxCallback {
    void onOptWxReq(BaseReq baseReq);
    void onOptWxResp(BaseResp baseResp);
}
