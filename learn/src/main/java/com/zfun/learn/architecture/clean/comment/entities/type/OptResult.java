package com.zfun.learn.architecture.clean.comment.entities.type;

public abstract class OptResult<T> {
    public final int resultCode;
    public final String optResultMsg;
    public final T result;

    public OptResult(int resultCode, String optResultMsg, T result) {
        this.resultCode = resultCode;
        this.optResultMsg = optResultMsg;
        this.result = result;
    }

    abstract public boolean isOk();
}
