package com.zfun.learn.architecture.clean.comment.entities;

public class CommentProvider {




    //single instance
    //利用JVM类的加载机制，实现同步
    private CommentProvider() {
    }

    private static class Holder {
        private static final CommentProvider sInstance = new CommentProvider();
    }

    public static CommentProvider getInstance() {
        return Holder.sInstance;
    }
}
