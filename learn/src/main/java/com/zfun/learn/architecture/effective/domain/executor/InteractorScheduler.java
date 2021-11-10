package com.zfun.learn.architecture.effective.domain.executor;

import com.zfun.learn.architecture.effective.domain.interactor.Interactor;

public interface InteractorScheduler {
    void execute(Runnable interactor);

    <P extends Interactor.ResponseValues> void notifyResponse(P responseValues, Interactor.OnCallback<P> callback);

    <P extends Interactor.ResponseValues> void onError(Interactor.OnCallback<P> callback);
}
