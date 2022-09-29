package com.zfun.learn.architecture.clean.comment.entities;

public interface ITools {
    void runMainThread(Runnable runnable);

    void runThread(Runnable runnable);

    void release();
}
