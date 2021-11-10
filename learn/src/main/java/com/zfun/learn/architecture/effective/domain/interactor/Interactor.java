package com.zfun.learn.architecture.effective.domain.interactor;

public abstract class Interactor<Q extends Interactor.RequestValues,P extends Interactor.ResponseValues> {
    private Q requestValues;
    private OnCallback<P> callback;

    public void setRequestValues(Q requestValues) {
        this.requestValues = requestValues;
    }

    public Q getRequestValues() {
        return requestValues;
    }

    public void setCallback(OnCallback<P> callback) {
        this.callback = callback;
    }

    public OnCallback<P> getCallback() {
        return callback;
    }

    public void run(){
        execute(requestValues);
    }

    abstract void execute(Q requestValues);



    public interface RequestValues{

    }

    public interface ResponseValues{

    }

    public interface OnCallback<P>{
        void onSuccess(P response);
        void onFail();
    }
}
