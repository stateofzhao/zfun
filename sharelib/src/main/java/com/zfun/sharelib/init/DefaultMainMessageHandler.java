package com.zfun.sharelib.init;

import android.os.Handler;

import java.lang.ref.WeakReference;

/**
 * Created by lzf on 2021/12/21 5:18 下午
 */
public class DefaultMainMessageHandler implements IMessageHandler {

    public static IMessageHandler get(){
        return new DefaultMainMessageHandler();
    }

    private final Handler mainHandler;
    private DefaultMainMessageHandler(){
        mainHandler = new Handler(InternalShareInitBridge.getInstance().getApplicationContext().getMainLooper());
    }
    @Override
    public void asyncRunInMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    @Override
    public void runInOtherThread(Runnable runnable) {
        new Thread(runnable).start();
    }
}
