package com.zfun.sharelib.init;

import java.lang.ref.WeakReference;

/**
 * Created by lzf on 2021/12/21 3:10 下午
 */
public class NullableDebugCheck implements IDebugCheck {
    private static WeakReference<IDebugCheck> weakReference;

    public static synchronized IDebugCheck get(){
        if(null == weakReference || null == weakReference.get()){
            weakReference = new WeakReference<>(new NullableDebugCheck());
        }
        return weakReference.get();
    }

    @Override
    public void classicAssert(boolean value, Throwable info) {

    }
}
