package com.zfun.sharelib.core;

import androidx.annotation.NonNull;

/**
 * 酷我好友分享
 * <p/>
 * Created by lizhaofei on 2017/8/16 17:30
 */
public class KwFriendHandler implements IShareHandler {

    @Override
    public void share(@NonNull ShareData shareData) {
        //由于不需要特殊处理，仅仅是做一次跳转，所以直接下放到业务逻辑中了，此Handler仅仅作为占位，
        // 如果后期这块复杂了，就需要在ShareData中建立对应的分享数据，然后将分享逻辑放到这里来处理
    }

    @Override
    public boolean isSupport() {
        return true;
    }

    @Override
    public void init() {
    }

    @Override
    public void release() {
    }
}
