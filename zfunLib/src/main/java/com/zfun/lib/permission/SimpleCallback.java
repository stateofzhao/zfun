package com.zfun.lib.permission;

import com.zfun.lib.permission.core.Callback;

/**
 * 缩放一下{@link Callback}
 * <p/>
 * Created by lizhaofei on 2018/3/14 11:47
 */
public abstract class SimpleCallback implements Callback {

    @Override
    public void onCancel(int requestCode) {
        //注意，这个是系统调用，一般不需要处理
    }
}