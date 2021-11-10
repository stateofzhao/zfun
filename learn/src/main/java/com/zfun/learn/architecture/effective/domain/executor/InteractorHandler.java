package com.zfun.learn.architecture.effective.domain.executor;

import com.zfun.learn.architecture.effective.domain.interactor.Interactor;

import java.util.concurrent.atomic.AtomicInteger;

public class InteractorHandler {
    private InteractorScheduler mInteractorSheduler;

    private final AtomicInteger counter = new AtomicInteger(0);

    public <R extends Interactor.RequestValues, P extends Interactor.ResponseValues> void execute(final R request, final Interactor<R, P> interactor, final Interactor.OnCallback<P> callback) {
        counter.incrementAndGet();
        interactor.setRequestValues(request);
        interactor.setCallback(new UiWrapperCallback<P>(callback));
        mInteractorSheduler.execute(new Runnable() {
            @Override
            public void run() {
                interactor.run();
                counter.decrementAndGet();
            }
        });
    }

    private class UiWrapperCallback<P extends Interactor.ResponseValues> implements Interactor.OnCallback<P> {
        private final Interactor.OnCallback<P> oriCallback;

        private UiWrapperCallback(Interactor.OnCallback<P> oriCallback) {
            this.oriCallback = oriCallback;
        }

        @Override
        public void onSuccess(P response) {
            mInteractorSheduler.notifyResponse(response, oriCallback);
        }

        @Override
        public void onFail() {
            mInteractorSheduler.onError(oriCallback);
        }
    }

    private InteractorHandler() {
    }

    private InteractorHandler(InteractorScheduler interactorSheduler) {
        mInteractorSheduler = interactorSheduler;
    }

    private static InteractorHandler INSTANCE;

    public static InteractorHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (InteractorHandler.class) {
                if (null == INSTANCE) {
                    INSTANCE = new InteractorHandler(new InteractorThreadSheduler());
                }
            }
        }
        return INSTANCE;
    }
}
