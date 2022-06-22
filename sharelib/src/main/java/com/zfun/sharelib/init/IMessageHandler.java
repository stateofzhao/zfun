package com.zfun.sharelib.init;

/**
 * Created by lzf on 2021/12/21 5:15 下午
 */
public interface IMessageHandler {
    void asyncRunInMainThread(Runnable runnable);
    void runInOtherThread(Runnable runnable);
}
