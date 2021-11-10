package com.zfun.learn.architecture.effective.domain.executor;

import android.os.Handler;
import android.os.Looper;
import com.zfun.learn.architecture.effective.domain.interactor.Interactor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InteractorThreadSheduler implements InteractorScheduler {
    private ExecutorService executorService;
    private Handler handler;

    public InteractorThreadSheduler(){
        executorService = Executors.newScheduledThreadPool(10);
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void execute(Runnable interactor) {
        executorService.execute(interactor);
    }

    @Override
    public <P extends Interactor.ResponseValues> void notifyResponse(final P responseValues, final Interactor.OnCallback<P> callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(responseValues);
            }
        });
    }

    @Override
    public <P extends Interactor.ResponseValues> void onError(final Interactor.OnCallback<P> callback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFail();
            }
        });
    }
}
