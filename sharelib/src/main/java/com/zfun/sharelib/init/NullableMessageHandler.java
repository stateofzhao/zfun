package com.zfun.sharelib.init;

import android.os.Handler;

import java.lang.ref.WeakReference;

/**
 * Created by lzf on 2021/12/21 5:18 下午
 */
public class NullableMessageHandler implements IMessageHandler{
    private static WeakReference<IMessageHandler> weakReference;

    public static synchronized IMessageHandler get(){
        if(null == weakReference || null == weakReference.get()){
            weakReference = new WeakReference<>(new NullableMessageHandler());
        }
        return weakReference.get();
    }

    private final Handler mainHandler;
    private NullableMessageHandler(){
        mainHandler = new Handler(InitContext.getInstance().getHostActivity().getMainLooper());
    }
    @Override
    public void asyncRun(Runnable runnable) {
        mainHandler.post(runnable);
    }
}
